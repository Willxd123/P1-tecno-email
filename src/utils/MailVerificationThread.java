package utils;

import CapaPresentacion.controllers.CartillaController;
import CapaPresentacion.controllers.EnvaseController;
import CapaPresentacion.controllers.InsumoController;
import CapaPresentacion.controllers.PagoController;
import CapaPresentacion.controllers.PedidoController;
import CapaPresentacion.controllers.ProductoController;
import CapaPresentacion.controllers.ReporteController;
import CapaPresentacion.PlantillaBase;
import CapaPresentacion.controllers.RolController;
import CapaPresentacion.controllers.UsuarioController;
import configuracion.Configuracion;
import utils.analex.Analex;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Hilo principal de verificación y procesamiento de correos vía Sockets TCP puros.
 * Se conecta a POP3 (puerto 110) para leer comandos y responde vía SMTP (puerto 25).
 */
public class MailVerificationThread extends Thread {

    private static final String END = "\r\n";
    private boolean running = true;
    private final int sleepIntervalMs = 10000; // 10 segundos

    public MailVerificationThread() {
        super("MailVerificationThread");
    }

    public void detener() {
        this.running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        System.out.println("[MailThread] Hilo de procesamiento de correos iniciado.");
        while (running) {
            try {
                procesarCorreos();
            } catch (Exception e) {
                System.err.println("[MailThread] ERROR general en ciclo: " + e.getMessage());
            }

            try {
                Thread.sleep(sleepIntervalMs);
            } catch (InterruptedException e) {
                System.out.println("[MailThread] Hilo interrumpido.");
                break;
            }
        }
        System.out.println("[MailThread] Hilo de procesamiento de correos detenido.");
    }

    /**
     * Se conecta por POP3, descarga los correos, los procesa y los responde.
     */
    private void procesarCorreos() {
        String host = Configuracion.getPopHost();
        int port = Configuracion.getPopPort();
        String user = Configuracion.getPopUser();
        String pass = Configuracion.getPopPassword();

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream writer = new DataOutputStream(socket.getOutputStream())) {

            // Leer respuesta inicial del servidor
            String line = reader.readLine();
            if (line == null || !line.startsWith("+OK")) {
                System.err.println("[POP3] Error en respuesta inicial: " + line);
                return;
            }

            // USER
            writer.writeBytes(Command.user(user));
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] Error en USER: " + line);
                return;
            }

            // PASS
            writer.writeBytes(Command.pass(pass));
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] Error en PASS: " + line);
                return;
            }

            // STAT (obtener total de mensajes y tamaño)
            writer.writeBytes(Command.stat());
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] Error en STAT: " + line);
                return;
            }

            String[] statParts = line.split(" ");
            int totalMensajes = Integer.parseInt(statParts[1]);

            if (totalMensajes == 0) {
                // No hay correos por procesar, salir ordenadamente
                writer.writeBytes(Command.quit());
                reader.readLine();
                return;
            }

            System.out.println("[POP3] Hay " + totalMensajes + " correos nuevos en la bandeja de entrada.");

            // Procesar los correos
            for (int i = 1; i <= totalMensajes; i++) {
                System.out.println("[POP3] Recuperando correo #" + i + "...");
                
                // RETR
                writer.writeBytes(Command.retr(i));
                line = reader.readLine();
                if (!line.startsWith("+OK")) {
                    System.err.println("[POP3] Error al recuperar correo #" + i + ": " + line);
                    continue;
                }

                // Leer todo el cuerpo del correo hasta la línea con un único punto "."
                StringBuilder rawMail = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (line.equals(".")) {
                        break;
                    }
                    rawMail.append(line).append("\n");
                }

                // Extraer datos usando Extractor
                Email emailObj = Extractor.getEmail(rawMail.toString());
                String from = emailObj.getFrom();
                String subject = emailObj.getSubject();

                System.out.println("[MailThread] Correo recibido de: " + from + " | Asunto: " + subject);

                // Procesar comando
                String commandName = Analex.getComando(subject);
                List<String> params = Analex.getParametros(subject);

                // Verificar que el usuario exista (Autenticación por Email) - DESACTIVADO TEMPORALMENTE PARA FACILITAR PRUEBAS
                boolean emailValido = true;
                /*
                if (commandName != null) {
                    String cmdUpper = commandName.toUpperCase().trim();
                    // Permitimos el registro (CU1-01) para nuevos usuarios
                    if (!cmdUpper.equals("CU1-01") && !cmdUpper.equals("REGISTRAR_USUARIO") && !cmdUpper.equals("INSPER")) {
                        CapaDatos.DUsuarios userAuth = CapaDatos.DUsuarios.obtenerPorEmail(from);
                        if (userAuth == null) {
                            emailValido = false;
                            System.out.println("[MailThread] Correo no registrado en el sistema: " + from);
                            String errorAuth = "Error de Autenticación: Tu correo (" + from + ") no está registrado en el sistema ZUZU.\n" +
                                               "No estás autorizado para realizar consultas o peticiones.\n" +
                                               "Por favor, comunícate con un administrador o regístrate mediante el comando CU1-01.";
                            boolean sent = enviarRespuestaSMTP(from, subject, errorAuth);
                            if (sent) {
                                writer.writeBytes(Command.dele(i));
                                reader.readLine();
                            }
                        }
                    }
                }

                if (!emailValido) {
                    continue; // Saltar al siguiente correo
                }
                */

                String responseMessage = ejecutarComando(commandName, params);

                // Enviar respuesta por SMTP
                System.out.println("[MailThread] Enviando respuesta vía SMTP a " + from + "...");
                boolean sent = enviarRespuestaSMTP(from, subject, responseMessage);

                if (sent) {
                    // Si se procesó y respondió bien, marcar para eliminar
                    writer.writeBytes(Command.dele(i));
                    line = reader.readLine();
                    if (line.startsWith("+OK")) {
                        System.out.println("[POP3] Correo #" + i + " marcado para eliminación.");
                    }
                }
            }

            // QUIT para consolidar eliminaciones
            writer.writeBytes(Command.quit());
            reader.readLine();

        } catch (Exception e) {
            System.err.println("[MailThread] ERROR en conexión POP3: " + e.getMessage());
        }
    }

    /**
     * Enrutador de comandos hacia los controladores específicos de cada recurso.
     */
    private String ejecutarComando(String comando, List<String> parametros) {
        if (comando == null || comando.trim().isEmpty()) {
            return "Error: Asunto vacío. Por favor envíe un comando válido.";
        }

        comando = comando.toUpperCase().trim();

        // 1. Recurso: Usuarios
        if (UsuarioController.canHandle(comando)) {
            return UsuarioController.handle(comando, parametros);
        }

        // 2. Recurso: Roles
        if (RolController.canHandle(comando)) {
            return RolController.handle(comando, parametros);
        }

        // 3. CU2: Insumos y Recetas
        if (InsumoController.canHandle(comando)) {
            return InsumoController.handle(comando, parametros);
        }

        // 4. CU3: Envases
        if (EnvaseController.canHandle(comando)) {
            return EnvaseController.handle(comando, parametros);
        }

        // 5. CU4: Cartilla del Cliente
        if (CartillaController.canHandle(comando)) {
            return CartillaController.handle(comando, parametros);
        }

        // 6. CU5: Ventas / Productos
        if (ProductoController.canHandle(comando)) {
            return ProductoController.handle(comando, parametros);
        }

        // 7. CU6: Pedidos
        if (PedidoController.canHandle(comando)) {
            return PedidoController.handle(comando, parametros);
        }

        // 8. CU7: Pagos
        if (PagoController.canHandle(comando)) {
            return PagoController.handle(comando, parametros);
        }

        // 9. CU8: Reportes
        if (ReporteController.canHandle(comando)) {
            return ReporteController.handle(comando, parametros);
        }

        // 10. Ayuda general
        if (comando.equals("HELP") || comando.equals("AYUDA")) {
            return obtenerAyuda();
        }

        return "Error: Comando '" + comando + "' no reconocido por el sistema.\n" +
               "Envíe 'HELP' en el Asunto para ver la lista de comandos disponibles.";
    }

    /**
     * Envía un correo electrónico utilizando sockets TCP puros en el puerto 25.
     */
    private boolean enviarRespuestaSMTP(String destinatario, String subjectOriginal, String contenido) {
        String host = Configuracion.getSmtpHost();
        int port = Configuracion.getSmtpPort();
        String remitente = Configuracion.getSmtpMail();

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream writer = new DataOutputStream(socket.getOutputStream())) {

            // Respuesta de bienvenida del servidor
            String line = reader.readLine();
            if (!line.startsWith("220")) return false;

            // HELO
            writer.writeBytes("HELO mail.tecnoweb.org.bo" + END);
            line = reader.readLine();
            if (!line.startsWith("250")) return false;

            // MAIL FROM
            writer.writeBytes("MAIL FROM:<" + remitente + ">" + END);
            line = reader.readLine();
            if (!line.startsWith("250")) return false;

            // RCPT TO
            writer.writeBytes("RCPT TO:<" + destinatario + ">" + END);
            line = reader.readLine();
            if (!line.startsWith("250")) return false;

            // DATA
            writer.writeBytes("DATA" + END);
            line = reader.readLine();
            if (!line.startsWith("354")) return false;

            // Cuerpo del mensaje (Cabeceras + Contenido)
            writer.writeBytes("From: " + remitente + END);
            writer.writeBytes("To: " + destinatario + END);
            writer.writeBytes("Subject: Re: " + subjectOriginal + END);
            writer.writeBytes("MIME-Version: 1.0" + END);
            
            // Cuerpo (Detección dinámica HTML vs Texto Plano)
            if (contenido.trim().startsWith("<!DOCTYPE html>") || contenido.trim().startsWith("<html>")) {
                writer.writeBytes("Content-Type: text/html; charset=UTF-8" + END);
                writer.writeBytes(END); // Separador cabecera-cuerpo
                writer.writeBytes(contenido);
            } else {
                writer.writeBytes("Content-Type: text/plain; charset=UTF-8" + END);
                writer.writeBytes(END); // Separador cabecera-cuerpo
                writer.writeBytes("--- RESPUESTA AUTOMÁTICA DEL SISTEMA (GRUPO 16) ---\n\n");
                writer.writeBytes(contenido);
                writer.writeBytes("\n\n-------------------------------------------------\n");
                writer.writeBytes("TecnoEmailZUZU v1.0 - Repostería Automatizada\n");
            }
            
            // Fin de DATA
            writer.writeBytes(END + "." + END);
            line = reader.readLine();
            if (!line.startsWith("250")) return false;

            // QUIT
            writer.writeBytes("QUIT" + END);
            reader.readLine();

            return true;

        } catch (Exception e) {
            System.err.println("[SMTP] ERROR al enviar correo: " + e.getMessage());
            return false;
        }
    }

    private String obtenerAyuda() {
        String contenido =
            "<h2 class=\"card-title\">Referencia de Comandos del Sistema</h2>" +
            "<p style=\"color:#61381c;font-size:13px;margin-bottom:20px;\">Envía el comando en el <strong>Asunto</strong> del correo a <strong>grupo16sc@tecnoweb.org.bo</strong>.<br>" +
            "Formato: <code>COMANDO[\"param1\",\"param2\"]</code> &nbsp;|&nbsp; Si el comando es incorrecto recibirás un mensaje de error descriptivo.</p>" +
            seccionAyuda("CU1 — Gestión de Usuarios", new String[][]{
                {"CU1_01 / INSPER", "[\"Nombre\",\"Apellido\",\"Tel\",\"Email\",\"Pass\",\"Rol\"]", "Registrar nuevo usuario. Roles: Propietario, Secretaria, Cliente"},
                {"CU1_02 / EDITAR_USUARIO", "[\"ID\",\"Nombre\",\"Apellido\",\"Tel\",\"Email\"]", "Editar datos personales de un usuario"},
                {"CU1_03 / CAMBIAR_PASSWORD", "[\"ID\",\"NuevaPass\"]", "Cambiar contraseña (mín. 6 caracteres)"},
                {"CU1_04 / DESACTIVAR_USUARIO", "[\"ID\"]", "Desactivar usuario sin eliminarlo"},
                {"CU1_05 / LISPER", "", "Listar todos los usuarios del sistema"},
                {"CU1_06 / BUSCAR_USUARIO", "[\"Texto\"]", "Buscar por nombre, apellido o teléfono"},
                {"CU1_07 / VER_PERFIL", "[\"ID\"]", "Ver perfil completo de un usuario"}
            }) +
            seccionAyuda("CU2 — Insumos y Recetas", new String[][]{
                {"CU2_01 / REGINSM", "[\"nombre\",\"unidad\",\"stock_ini\",\"stock_min\",\"costo\"]", "Registrar insumo. Unidades: kg, g, l, ml, unidad"},
                {"CU2_02 / EDTINSM", "[\"id\",\"nombre\",\"costo\",\"stock_min\"]", "Editar nombre, costo o stock mínimo de un insumo"},
                {"CU2_03 / LISINSM", "", "Listar todos los insumos con estado de stock"},
                {"CU2_04 / ENTINSM", "[\"id\",\"cantidad\",\"descripcion\"]", "Registrar entrada de stock (compra/reposición)"},
                {"CU2_05 / AJUINSM", "[\"id\",\"cant_nueva\",\"descripcion\"]", "Ajustar stock a un valor exacto (corrección)"},
                {"CU2_06 / MERINSM", "[\"id\",\"cantidad\",\"descripcion\"]", "Registrar merma o pérdida de insumo"},
                {"CU2_07 / HISINSM", "[\"insumo_id\"]", "Ver historial de movimientos de un insumo"},
                {"CU2_08 / ALEREP", "", "Ver insumos con stock por debajo del mínimo"},
                {"CU2_09 / REGREC", "[\"prod_id\",\"nombre\",\"desc\",\"ins_id:cant\",...]", "Registrar receta de un producto"},
                {"CU2_10 / EDTREC", "[\"receta_id\",\"ins_id:cant\",...]", "Reemplazar insumos de una receta existente"},
                {"CU2_11 / VERREC", "[\"producto_id\"]", "Ver receta e ingredientes de un producto"}
            }) +
            seccionAyuda("CU3 — Gestión de Envases", new String[][]{
                {"CU3_01 / REGENV", "[\"nombre\",\"descripcion\",\"stock_total\"]", "Registrar nuevo tipo de envase reutilizable"},
                {"CU3_02 / EDTENV", "[\"id\",\"nombre\",\"descripcion\"]", "Editar datos de un tipo de envase"},
                {"CU3_03 / LISENV", "", "Ver stock total, disponible y prestados de cada envase"},
                {"CU3_04 / PRESENV", "[\"pedido_id\",\"envase_id\",\"cantidad\"]", "Registrar préstamo de envases al entregar un pedido"},
                {"CU3_05 / DEVENV", "[\"pedido_origen_id\",\"envase_id\",\"cantidad\"]", "Registrar devolución de envases"},
                {"CU3_06 / PENDENV", "", "Ver todos los envases sin devolver por cliente"},
                {"CU3_07 / HISENV", "[\"usuario_id\"]", "Ver historial de préstamos/devoluciones de un cliente"}
            }) +
            seccionAyuda("CU4 — Cartilla del Cliente", new String[][]{
                {"CU4_01 / CARTILLA", "[\"usuario_id\"]", "Ver historial completo de pedidos de un cliente"},
                {"CU4_02 / BUSCART", "[\"texto\"]", "Buscar cliente por nombre/apellido para ver su cartilla"},
                {"CU4_03 / DETCART", "[\"pedido_id\"]", "Ver desglose de productos y precios de un pedido"},
                {"CU4_04 / CUOTACART", "[\"pedido_id\"]", "Ver cuotas y estados de pago de un pedido"},
                {"CU4_05 / ENVCART", "[\"pedido_id\"]", "Ver envases prestados y devueltos de un pedido"}
            }) +
            seccionAyuda("CU5 — Gestión de Productos", new String[][]{
                {"CU5_01 / REGPRO", "[\"nombre\",\"descripcion\",\"precio_unitario\"]", "Registrar nuevo producto al catálogo"},
                {"CU5_02 / EDTPRO", "[\"id\",\"nombre\",\"descripcion\",\"precio\"]", "Editar datos de un producto"},
                {"CU5_03 / TOGPRO", "[\"id\"]", "Activar o desactivar un producto del catálogo"},
                {"CU5_04 / LISPRO", "", "Listar todos los productos con precios y disponibilidad"},
                {"CU5_05 / COSTPRO", "[\"producto_id\"]", "Ver costo de producción y margen de un producto"},
                {"CU5_06 / DISPRO", "[\"producto_id\",\"cantidad\"]", "Verificar si hay insumos suficientes para producir N unidades"}
            }) +
            seccionAyuda("CU6 — Gestión de Pedidos", new String[][]{
                {"CU6_01 / CRPEDIDO", "[\"usr_id\",\"contado\",\"prod_id:cant\",...]", "Crear pedido al contado. Ej: CRPEDIDO[\"1\",\"contado\",\"10:2\",\"11:1\"]"},
                {"CU6_01 / CRPEDIDO", "[\"usr_id\",\"cuotas\",\"n\",\"f1;f2\",\"prod:cant\",...]", "Crear pedido en cuotas. Ej: CRPEDIDO[\"1\",\"cuotas\",\"2\",\"2026-07-01;2026-08-01\",\"10:1\"]"},
                {"CU6_02 / DETPEDIDO", "[\"pedido_id\"]", "Ver detalle completo de un pedido"},
                {"CU6_03 / LISPEDIDO", "[filtro_opcional]", "Listar pedidos. Filtro: usuario_id ó estado (pendiente/pagado/entregado/cancelado)"},
                {"CU6_04 / EDTPEDIDO", "[\"pedido_id\",\"estado\"]", "Cambiar estado. Estados: pendiente, pagado, entregado, cancelado"},
                {"CU6_05 / CELPEDIDO", "[\"pedido_id\"]", "Cancelar pedido y restaurar stock de insumos"},
                {"CU6_06 / ENTPEDIDO", "[\"pedido_id\"]", "Confirmar entrega del pedido"}
            }) +
            seccionAyuda("CU7 — Gestión de Pagos", new String[][]{
                {"CU7_01 / PAGCONTADO", "[\"pedido_id\"]", "Confirmar recepción de pago al contado → pedido pasa a 'pagado'"},
                {"CU7_02 / PAGCUOTAS", "[\"pedido_id\",\"num_cuotas\",\"fecha1;fecha2;...\"]", "Configurar plan de cuotas para un pedido"},
                {"CU7_03 / PAGCUOTA", "[\"cuota_id\"]", "Marcar una cuota como pagada"},
                {"CU7_04 / VISCUOTAS", "[\"pedido_id\"]", "Ver todas las cuotas de un pedido"},
                {"CU7_05 / CUOVENC", "", "Ver cuotas vencidas y sin pagar de todos los clientes"},
                {"CU7_06 / CUOPROX", "[\"dias\"]", "Ver cuotas que vencen en los próximos N días"},
                {"CU7_07 / RESPAGCLI", "[\"usuario_id\"]", "Ver resumen de pagos de un cliente"}
            }) +
            seccionAyuda("CU8 — Reportes y Estadísticas", new String[][]{
                {"CU8_01 / REPVENTAS", "[\"YYYY-MM-DD\",\"YYYY-MM-DD\"]", "Reporte de ventas en un período"},
                {"CU8_02 / REPINGRES", "[\"YYYY-MM-DD\",\"YYYY-MM-DD\"]", "Ingresos contado vs crédito en un período"},
                {"CU8_03 / REPCUOPEND", "", "Total de cuotas pendientes por cobrar"},
                {"CU8_04 / REPCONINS", "[\"YYYY-MM-DD\",\"YYYY-MM-DD\"]", "Consumo de insumos en un período"},
                {"CU8_05 / REPCOSTO", "[\"YYYY-MM-DD\",\"YYYY-MM-DD\"]", "Costo de producción vs ingresos (margen)"},
                {"CU8_06 / REPSTOCK", "", "Insumos con stock por debajo del mínimo"},
                {"CU8_07 / REPENVPRES", "", "Envases actualmente prestados a clientes"},
                {"CU8_08 / REPCLIFRE", "[\"YYYY-MM-DD\",\"YYYY-MM-DD\"]", "Ranking de clientes más frecuentes"},
                {"CU8_09 / REPPROVEND", "[\"YYYY-MM-DD\",\"YYYY-MM-DD\"]", "Ranking de productos más vendidos"}
            }) +
            "<p style=\"font-size:12px;color:#8c7b70;margin-top:20px;\">&#9888; Si recibes un error, el mensaje indicará exactamente qué parámetro es incorrecto o falta.</p>";

        return PlantillaBase.construirPlantillaBase("Ayuda - Repostería ZUZU", contenido);
    }

    private String seccionAyuda(String titulo, String[][] filas) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3 style=\"color:#8b5a2b;font-size:15px;margin:20px 0 8px;\">").append(titulo).append("</h3>");
        sb.append("<table>");
        sb.append("<tr><th>Comando</th><th>Parámetros</th><th>Descripción</th></tr>");
        for (String[] fila : filas) {
            sb.append("<tr>")
              .append("<td style=\"font-family:monospace;font-size:12px;white-space:nowrap;\">").append(fila[0]).append("</td>")
              .append("<td style=\"font-family:monospace;font-size:11px;color:#61381c;\">").append(fila[1]).append("</td>")
              .append("<td>").append(fila[2]).append("</td>")
              .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }
}

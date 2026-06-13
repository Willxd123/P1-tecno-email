package utils;

import CapaPresentacion.controladores.RolControlador;
import CapaPresentacion.controladores.UsuarioControlador;
import CapaPresentacion.controladores.InsumoControlador;
import CapaPresentacion.controladores.EnvaseControlador;
import CapaPresentacion.controladores.ProductoControlador;
import CapaPresentacion.controladores.PedidoControlador;
import CapaPresentacion.controladores.CartillaControlador;
import CapaPresentacion.controladores.ReporteControlador;
import CapaPresentacion.PAyuda;
import configuracion.Configuracion;
import utils.analex.Analex;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Hilo principal de verificaciÃ³n y procesamiento de correos vÃ­a Sockets TCP puros.
 * Se conecta a POP3 (puerto 110) para leer comandos y responde vÃ­a SMTP (puerto 25).
 */
public class HiloVerificacionCorreo extends Thread {

    private static final String END = "\r\n";
    private boolean running = true;
    private final int sleepIntervalMs = 10000; // 10 segundos

    public HiloVerificacionCorreo() {
        super("HiloVerificacionCorreo");
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
    }     /**
     * Se conecta por POP3, descarga los correos, los procesa y los responde.
     */
    private void procesarCorreos() {
        // Reconciliar primero los pagos por QR pendientes
        try {
            reconciliarPagosQR();
        } catch (Exception e) {
            System.err.println("[MailThread] Error al reconciliar pagos QR: " + e.getMessage());
        }

        String host = Configuracion.getPopHost();
        int port = Configuracion.getPopPort();
        String user = Configuracion.getPopUser();
        String pass = Configuracion.getPopPassword();

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        String timeStr = java.time.LocalTime.now().format(dtf);

        System.out.println("\n[" + timeStr + "] ─── INICIANDO VERIFICACIÓN DE CORREOS ───");
        System.out.println("[POP3] Conectando a " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream writer = new DataOutputStream(socket.getOutputStream())) {

            // Leer respuesta inicial del servidor
            String line = reader.readLine();
            if (line == null || !line.startsWith("+OK")) {
                System.err.println("[POP3] ConexiÃ³n fallida. Respuesta inicial del servidor: " + line);
                return;
            }
            System.out.println("[POP3] ConexiÃ³n establecida.");

            // USER
            System.out.println("[POP3] Iniciando sesiÃ³n para el usuario: " + user + "...");
            writer.writeBytes(Comando.user(user));
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] ERROR en comando USER: " + line);
                return;
            }

            // PASS
            writer.writeBytes(Comando.pass(pass));
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] ERROR en contraseÃ±a (PASS): " + line);
                return;
            }
            System.out.println("[POP3] SesiÃ³n iniciada con Ã©xito.");

            // STAT (obtener total de mensajes y tamaÃ±o)
            writer.writeBytes(Comando.stat());
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] ERROR en comando STAT: " + line);
                return;
            }

            String[] statParts = line.split(" ");
            int totalMensajes = Integer.parseInt(statParts[1]);

            if (totalMensajes == 0) {
                System.out.println("[POP3] Bandeja vacÃ­a. No hay correos nuevos por procesar.");
                writer.writeBytes(Comando.quit());
                reader.readLine();
                System.out.println("[POP3] ConexiÃ³n cerrada.");
                System.out.println("[" + java.time.LocalTime.now().format(dtf) + "] â”€â”€â”€ FIN DE LA VERIFICACIÃ“N â”€â”€â”€");
                return;
            }

            System.out.println("[POP3] Bandeja de entrada: " + totalMensajes + " correo(s) por procesar.");

            // Procesar los correos
            for (int i = 1; i <= totalMensajes; i++) {
                System.out.println("\n[POP3] -- Procesando Correo #" + i + " de " + totalMensajes + " --");
                
                // RETR
                writer.writeBytes(Comando.retr(i));
                line = reader.readLine();
                if (!line.startsWith("+OK")) {
                    System.out.println("  [ERROR] al recuperar correo #" + i + ": " + line);
                    continue;
                }

                // Leer todo el cuerpo del correo hasta la lÃ­nea con un Ãºnico punto "."
                StringBuilder rawMail = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (line.equals(".")) {
                        break;
                    }
                    rawMail.append(line).append("\n");
                }

                // Extraer datos usando Extractor
                Correo CorreoObj = Extractor.getCorreo(rawMail.toString());
                String from = CorreoObj.getFrom();
                String subject = CorreoObj.getSubject();

                System.out.println("  Remitente: " + from);
                System.out.println("  Asunto: " + subject);

                // Ignorar correos de respuesta o reenvÃ­o para evitar bucles infinitos
                String subjUpper = subject != null ? subject.toUpperCase().trim() : "";
                if (subjUpper.startsWith("RE:") || subjUpper.startsWith("FWD:") || subjUpper.startsWith("FW:")) {
                    System.out.println("  [POP3] Correo omitido por ser una respuesta/reenvÃ­o (evita bucles).");
                    writer.writeBytes(Comando.dele(i));
                    reader.readLine(); // Leer confirmaciÃ³n de DELE
                    continue;
                }

                String responseMessage = "";

                // Validar sintaxis y ejecutar
                try {
                    Analex.validarSintaxis(subject);
                    String ComandoName = Analex.getComando(subject);
                    List<String> params = Analex.getParametros(subject);
                    
                    System.out.println("  Comando detectado: " + ComandoName + " | ParÃ¡metros: " + params);
                    System.out.println("  Ejecutando controlador...");
                    responseMessage = ejecutarComando(ComandoName, params);
                } catch (IllegalArgumentException e) {
                    System.out.println("  [ERROR DE SINTAXIS] " + e.getMessage());
                    responseMessage = "Error: " + e.getMessage() + "\n\n" +
                                      "Formato esperado:\n" +
                                      "Para comandos con parÃ¡metros: COMANDO[\"param1\", \"param2\", ...]\n" +
                                      "Ejemplo: INSPER[\"4715292\", \"Juan Carlos\", ...]\n\n" +
                                      "Para comandos sin parÃ¡metros: COMANDO\n" +
                                      "Ejemplo: HELP";
                }

                // Enviar respuesta por SMTP
                System.out.println("  Enviando respuesta vÃ­a SMTP a: " + from + "...");
                boolean sent = enviarRespuestaSMTP(from, subject, responseMessage);

                if (sent) {
                    // Si se procesÃ³ y respondiÃ³ bien, marcar para eliminar
                    writer.writeBytes(Comando.dele(i));
                    line = reader.readLine();
                    if (line.startsWith("+OK")) {
                        System.out.println("    [POP3] Correo #" + i + " marcado para eliminaciÃ³n.");
                    }
                }
            }

            // QUIT para consolidar eliminaciones
            System.out.println("\n[POP3] Cerrando sesiÃ³n y consolidando eliminaciones...");
            writer.writeBytes(Comando.quit());
            reader.readLine();
            System.out.println("[POP3] ConexiÃ³n cerrada.");

        } catch (Exception e) {
            System.err.println("\n[MailThread] ERROR GENERAL en ciclo POP3: " + e.getMessage());
        }
        System.out.println("[" + java.time.LocalTime.now().format(dtf) + "] â”€â”€â”€ FIN DE LA VERIFICACIÃ“N â”€â”€â”€");
    }

    /**
     * Enrutador de comandos hacia los controladores especÃ­ficos de cada recurso.
     */
    private String ejecutarComando(String comando, List<String> parametros) {
        if (comando == null || comando.trim().isEmpty()) {
            return "Error: Asunto vacÃ­o. Por favor envÃ­e un comando vÃ¡lido.";
        }

        comando = comando.toUpperCase().trim();

        // 1. Recurso: Usuarios
        if (UsuarioControlador.canHandle(comando)) {
            return UsuarioControlador.handle(comando, parametros);
        }

        // 2. Recurso: Roles
        if (RolControlador.canHandle(comando)) {
            return RolControlador.handle(comando, parametros);
        }

        // 3. Recurso: Insumos
        if (InsumoControlador.canHandle(comando)) {
            return InsumoControlador.handle(comando, parametros);
        }

        // 4. Recurso: Envases
        if (EnvaseControlador.canHandle(comando)) {
            return EnvaseControlador.handle(comando, parametros);
        }

        // 5. Recurso: Productos/Recetas
        if (ProductoControlador.canHandle(comando)) {
            return ProductoControlador.handle(comando, parametros);
        }

        // 6. Recurso: Pedidos/Pagos
        if (PedidoControlador.canHandle(comando)) {
            return PedidoControlador.handle(comando, parametros);
        }

        // 7. Recurso: Cartilla
        if (CartillaControlador.canHandle(comando)) {
            return CartillaControlador.handle(comando, parametros);
        }

        // 8. Recurso: Reportes
        if (ReporteControlador.canHandle(comando)) {
            return ReporteControlador.handle(comando, parametros);
        }

        // 9. Ayuda general
        if (comando.equals("HELP") || comando.equals("AYUDA")) {
            return PAyuda.generarHtml();
        }

        return "Error: Comando '" + comando + "' no reconocido por el sistema.\n" +
               "EnvÃ­e 'HELP' en el Asunto para ver la lista de comandos disponibles.";
    }

    /**
     * EnvÃ­a un correo electrÃ³nico utilizando sockets TCP puros en el puerto 25.
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
            if (line == null || !line.startsWith("220")) {
                System.err.println("    [SMTP] ERROR de bienvenida: " + line);
                return false;
            }

            // HELO
            writer.writeBytes("HELO mail.tecnoweb.org.bo" + END);
            line = reader.readLine();
            if (line == null || !line.startsWith("250")) {
                System.err.println("    [SMTP] ERROR en HELO: " + line);
                return false;
            }

            // MAIL FROM
            writer.writeBytes("MAIL FROM:<" + remitente + ">" + END);
            line = reader.readLine();
            if (line == null || !line.startsWith("250")) {
                System.err.println("    [SMTP] ERROR en MAIL FROM: " + line);
                return false;
            }

            // RCPT TO
            writer.writeBytes("RCPT TO:<" + destinatario + ">" + END);
            line = reader.readLine();
            if (line == null || !line.startsWith("250")) {
                System.err.println("    [SMTP] ERROR en RCPT TO: " + line);
                return false;
            }

            // DATA
            writer.writeBytes("DATA" + END);
            line = reader.readLine();
            if (line == null || !line.startsWith("354")) {
                System.err.println("    [SMTP] ERROR en DATA: " + line);
                return false;
            }

            // Cuerpo del mensaje (Cabeceras + Contenido)
            writer.writeBytes("From: " + remitente + END);
            writer.writeBytes("To: " + destinatario + END);
            writer.writeBytes("Subject: Re: " + subjectOriginal + END);
            writer.writeBytes("MIME-Version: 1.0" + END);
            
            // Cuerpo (DetecciÃ³n dinÃ¡mica HTML vs Texto Plano)
            if (contenido.trim().startsWith("<!DOCTYPE html>") || contenido.trim().startsWith("<html>")) {
                writer.writeBytes("Content-Type: text/html; charset=UTF-8" + END);
                writer.writeBytes(END); // Separador cabecera-cuerpo
                
                // CRITICAL: Write raw UTF-8 bytes to socket to prevent char/emoji corruption
                byte[] htmlBytes = contenido.replace("\r\n", "\n").replace("\n", "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                writer.write(htmlBytes);
            } else {
                writer.writeBytes("Content-Type: text/plain; charset=UTF-8" + END);
                writer.writeBytes(END); // Separador cabecera-cuerpo
                writer.writeBytes("--- RESPUESTA AUTOMÃ TICA DEL SISTEMA (GRUPO 16) ---" + END + END);
                
                // CRITICAL: Write raw UTF-8 bytes to socket
                byte[] textBytes = contenido.replace("\r\n", "\n").replace("\n", "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
                writer.write(textBytes);
                
                writer.writeBytes(END + END + "-------------------------------------------------" + END);
                writer.writeBytes("TecnoCorreoZUZU v1.0 - ReposterÃ­a Automatizada" + END);
            }
            
            // Fin de DATA
            writer.writeBytes(END + "." + END);
            line = reader.readLine();
            if (line == null || !line.startsWith("250")) {
                System.out.println("    [SMTP] ERROR al finalizar DATA: " + line);
                return false;
            }

            // QUIT
            writer.writeBytes("QUIT" + END);
            line = reader.readLine();

            System.out.println("    [SMTP] Respuesta SMTP enviada y aceptada por el servidor.");
            return true;

        } catch (Exception e) {
            System.out.println("    [SMTP] ERROR al enviar correo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reconcilia las transacciones QR pendientes registradas en qr_transactions.json
     */
    private void reconciliarPagosQR() {
        java.util.Map<String, String> transacciones = PagoFacilService.cargarTransacciones();
        if (transacciones.isEmpty()) {
            return;
        }

        System.out.println("[MailThread] Reconciliando " + transacciones.size() + " transacción(es) pendiente(s) de QR...");

        // Lista de transacciones a remover del archivo JSON tras confirmar el pago
        java.util.List<String> transaccionesCompletadas = new java.util.ArrayList<>();

        for (java.util.Map.Entry<String, String> entry : transacciones.entrySet()) {
            String txId = entry.getKey();
            String info = entry.getValue();
            String[] parts = info.split(";");
            if (parts.length < 3) {
                continue;
            }
            String email = parts[0];
            double monto = Double.parseDouble(parts[1]);
            String tipo = parts[2];

            System.out.println("[MailThread] Consultando estado de " + txId + " en PagoFacil...");
            boolean pagado = PagoFacilService.consultarEstado(txId);

            if (pagado) {
                System.out.println("[MailThread] ¡Transacción " + txId + " PAGADA confirmada!");

                if (txId.startsWith("PED-")) {
                    try {
                        int pedidoId = Integer.parseInt(txId.substring(4));
                        boolean dbOk = CapaNegocio.NPedidos.confirmarPagoPedido(pedidoId);
                        
                        if (dbOk) {
                            String htmlContent = buildHtmlPedidoConfirmado(pedidoId, monto, txId);
                            enviarRespuestaSMTP(email, "CONFIRMACION DE PAGO PEDIDO #" + pedidoId, htmlContent);
                        } else {
                            System.err.println("[MailThread] Error al actualizar pedido " + pedidoId + " en la base de datos.");
                        }
                    } catch (Exception e) {
                        System.err.println("[MailThread] Error al procesar pago de pedido " + txId + ": " + e.getMessage());
                    }
                } else if (txId.startsWith("CUO-")) {
                    try {
                        int cuotaId = Integer.parseInt(txId.substring(4));
                        String resultMsg = CapaNegocio.NPedidos.confirmarPagoCuota(cuotaId);
                        
                        if (resultMsg.startsWith("Éxito") || resultMsg.startsWith("Advertencia")) {
                            String htmlContent = buildHtmlCuotaConfirmada(cuotaId, monto, txId, resultMsg);
                            enviarRespuestaSMTP(email, "CONFIRMACION DE PAGO CUOTA #" + cuotaId, htmlContent);
                        } else {
                            System.err.println("[MailThread] Error al confirmar cuota " + cuotaId + " en BD: " + resultMsg);
                        }
                    } catch (Exception e) {
                        System.err.println("[MailThread] Error al procesar pago de cuota " + txId + ": " + e.getMessage());
                    }
                }

                transaccionesCompletadas.add(txId);
            } else {
                System.out.println("[MailThread] Transacción " + txId + " sigue pendiente.");
            }
        }

        // Remover las que ya fueron pagadas
        for (String txId : transaccionesCompletadas) {
            PagoFacilService.removerTransaccion(txId);
        }
    }

    private String buildHtmlPedidoConfirmado(int pedidoId, double monto, String txId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\">");
        sb.append("<style>");
        sb.append("  body { font-family: 'Segoe UI', Roboto, sans-serif; background-color: #fdfaf7; color: #4a3e3d; margin: 0; padding: 0; }");
        sb.append("  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 40px rgba(97, 56, 28, 0.08); border: 1px solid #f0e6df; }");
        sb.append("  .header { background: linear-gradient(135deg, #10b981, #047857); padding: 35px 20px; text-align: center; color: #ffffff; }");
        sb.append("  .header h1 { margin: 0; font-size: 24px; font-weight: 700; text-shadow: 0 2px 4px rgba(0,0,0,0.15); }");
        sb.append("  .content { padding: 35px 30px; }");
        sb.append("  .card { background-color: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 16px; padding: 25px; text-align: center; margin-bottom: 25px; }");
        sb.append("  .success-icon { font-size: 48px; color: #10b981; margin-bottom: 15px; }");
        sb.append("  .amount { font-size: 28px; font-weight: 800; color: #047857; margin: 10px 0; }");
        sb.append("  .details-table { width: 100%; border-collapse: collapse; margin-top: 15px; }");
        sb.append("  .details-table td { padding: 10px; border-bottom: 1px solid #f0e6df; font-size: 14px; }");
        sb.append("  .details-table td.label { font-weight: 600; color: #6b7280; width: 40%; }");
        sb.append("  .details-table td.value { font-weight: 700; color: #4a3e3d; text-align: right; }");
        sb.append("  .footer { background-color: #fcf8f5; padding: 20px; text-align: center; font-size: 12px; color: #8c7b70; border-top: 1px solid #f0e6df; }");
        sb.append("</style></head><body>");
        sb.append("<div class=\"container\">");
        sb.append("  <div class=\"header\">");
        sb.append("    <img src=\"https://i.ibb.co/RpQ8WGhK/bienvenida.png\" alt=\"Chifones Peruanos Zuzú Logo\" style=\"max-height: 80px; margin-bottom: 12px; display: block; margin-left: auto; margin-right: auto;\">");
        sb.append("    <h1>🎉 PAGO CONFIRMADO 🎉</h1>");
        sb.append("  </div>");
        sb.append("  <div class=\"content\">");
        sb.append("    <div class=\"card\">");
        sb.append("      <div class=\"success-icon\">✓</div>");
        sb.append("      <h2 style=\"margin: 0; color: #166534; font-size: 20px;\">¡Pago Exitoso Recibido!</h2>");
        sb.append("      <p style=\"color: #15803d; font-size: 14px; margin: 5px 0 15px 0;\">Hemos recibido correctamente tu pago de:</p>");
        sb.append("      <div class=\"amount\">").append(monto).append(" Bs.</div>");
        sb.append("      <span style=\"background-color: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; padding: 6px 16px; border-radius: 30px; font-size: 12px; font-weight: 700; display: inline-block;\">PEDIDO TOTAL PAGADO</span>");
        sb.append("    </div>");
        sb.append("    <h3 style=\"margin-top: 0; border-bottom: 2px solid #f7efe9; padding-bottom: 8px; color: #047857;\">Detalle de Transacción</h3>");
        sb.append("    <table class=\"details-table\">");
        sb.append("      <tr><td class=\"label\">Pedido ID:</td><td class=\"value\">#").append(pedidoId).append("</td></tr>");
        sb.append("      <tr><td class=\"label\">PagoFacil Ref:</td><td class=\"value\">").append(txId).append("</td></tr>");
        sb.append("      <tr><td class=\"label\">Fecha de Pago:</td><td class=\"value\">").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date())).append("</td></tr>");
        sb.append("      <tr><td class=\"label\">Estado Pedido:</td><td class=\"value\"><span style=\"color: #10b981; font-weight: bold;\">PAGADO</span></td></tr>");
        sb.append("    </table>");
        sb.append("    <br><p style=\"text-align: center; font-size: 14px; color: #6b7280;\">Ya estamos preparando tu orden. ¡Muchas gracias por tu preferencia!</p>");
        sb.append("  </div>");
        sb.append("  <div class=\"footer\">");
        sb.append("    <strong>Grupo 16 - Tecnología Web (UAGRM)</strong><br>");
        sb.append("    Este es un correo de notificación automática de pagos.");
        sb.append("  </div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildHtmlCuotaConfirmada(int cuotaId, double monto, String txId, String resultMsg) {
        int pagoId = -1;
        int pedidoId = -1;
        double totalPedido = 0.0;
        try (java.sql.Connection conn = CapaDatos.Conexion.getConexion()) {
            String sqlCuota = "SELECT pago_id FROM cuotas WHERE id = ?";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCuota)) {
                ps.setInt(1, cuotaId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        pagoId = rs.getInt("pago_id");
                    }
                }
            }

            if (pagoId != -1) {
                String sqlPago = "SELECT pedido_id FROM pagos WHERE id = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlPago)) {
                    ps.setInt(1, pagoId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            pedidoId = rs.getInt("pedido_id");
                        }
                    }
                }
            }

            if (pedidoId != -1) {
                CapaDatos.DPedidos ped = CapaDatos.DPedidos.obtenerPorId(pedidoId);
                if (ped != null) {
                    totalPedido = ped.getTotal();
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("[MailThread] Error al consultar plan de cuotas: " + e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\">");
        sb.append("<style>");
        sb.append("  body { font-family: 'Segoe UI', Roboto, sans-serif; background-color: #fdfaf7; color: #4a3e3d; margin: 0; padding: 0; }");
        sb.append("  .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 40px rgba(97, 56, 28, 0.08); border: 1px solid #f0e6df; }");
        sb.append("  .header { background: linear-gradient(135deg, #10b981, #047857); padding: 35px 20px; text-align: center; color: #ffffff; }");
        sb.append("  .header h1 { margin: 0; font-size: 24px; font-weight: 700; text-shadow: 0 2px 4px rgba(0,0,0,0.15); }");
        sb.append("  .content { padding: 35px 30px; }");
        sb.append("  .card { background-color: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 16px; padding: 25px; text-align: center; margin-bottom: 25px; }");
        sb.append("  .success-icon { font-size: 48px; color: #10b981; margin-bottom: 15px; }");
        sb.append("  .amount { font-size: 28px; font-weight: 800; color: #047857; margin: 10px 0; }");
        sb.append("  .details-table, .cuotas-table { width: 100%; border-collapse: separate; border-spacing: 0; margin-top: 15px; border-radius: 12px; overflow: hidden; border: 1px solid #eedfd4; }");
        sb.append("  .details-table td { padding: 12px 16px; border-bottom: 1px solid #f7efe9; font-size: 14px; }");
        sb.append("  .details-table td.label { font-weight: 600; color: #6b7280; width: 40%; }");
        sb.append("  .details-table td.value { font-weight: 700; color: #4a3e3d; text-align: right; }");
        sb.append("  .cuotas-table th { background-color: #047857; color: #ffffff; padding: 12px 14px; font-size: 13px; font-weight: 600; text-align: left; }");
        sb.append("  .cuotas-table td { padding: 12px 14px; border-bottom: 1px solid #f7efe9; font-size: 13px; color: #4a3e3d; }");
        sb.append("  .cuotas-table tr:last-child td { border-bottom: none; }");
        sb.append("  .cuotas-table tr:nth-child(even) { background-color: #fdfbf9; }");
        sb.append("  .badge { display: inline-block; padding: 4px 10px; border-radius: 30px; font-size: 11px; font-weight: 600; text-align: center; }");
        sb.append("  .badge-si { background-color: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; }");
        sb.append("  .badge-pend { background-color: #fef3c7; color: #d97706; border: 1px solid #fde68a; }");
        sb.append("  .footer { background-color: #fcf8f5; padding: 20px; text-align: center; font-size: 12px; color: #8c7b70; border-top: 1px solid #f0e6df; }");
        sb.append("</style></head><body>");
        sb.append("<div class=\"container\">");
        sb.append("  <div class=\"header\">");
        sb.append("    <img src=\"https://i.ibb.co/RpQ8WGhK/bienvenida.png\" alt=\"Chifones Peruanos Zuzú Logo\" style=\"max-height: 80px; margin-bottom: 12px; display: block; margin-left: auto; margin-right: auto;\">");
        sb.append("    <h1>🎉 PAGO DE CUOTA CONFIRMADO 🎉</h1>");
        sb.append("  </div>");
        sb.append("  <div class=\"content\">");
        sb.append("    <div class=\"card\">");
        sb.append("      <div class=\"success-icon\">✓</div>");
        sb.append("      <h2 style=\"margin: 0; color: #166534; font-size: 18px;\">¡Cuota Pagada con Éxito!</h2>");
        sb.append("      <p style=\"color: #15803d; font-size: 13px; margin: 5px 0 15px 0;\">Hemos procesado tu pago de la Cuota #").append(cuotaId).append(" por:</p>");
        sb.append("      <div class=\"amount\">").append(monto).append(" Bs.</div>");
        sb.append("      <p style=\"font-size: 13px; font-weight: bold; color: #166534; margin: 10px 0 0 0;\">").append(resultMsg).append("</p>");
        sb.append("    </div>");
        
        sb.append("    <h3 style=\"margin-top: 25px; border-bottom: 2px solid #f7efe9; padding-bottom: 8px; color: #047857;\">Detalle de Transacción</h3>");
        sb.append("    <table class=\"details-table\">");
        sb.append("      <tr><td class=\"label\">Cuota ID:</td><td class=\"value\">#").append(cuotaId).append("</td></tr>");
        sb.append("      <tr><td class=\"label\">Pedido ID:</td><td class=\"value\">#").append(pedidoId).append("</td></tr>");
        sb.append("      <tr><td class=\"label\">Monto del Pedido:</td><td class=\"value\">").append(totalPedido).append(" Bs.</td></tr>");
        sb.append("      <tr><td class=\"label\">Referencia PagoFacil:</td><td class=\"value\">").append(txId).append("</td></tr>");
        sb.append("      <tr><td class=\"label\">Fecha de Pago:</td><td class=\"value\">").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date())).append("</td></tr>");
        sb.append("    </table>");

        if (pagoId != -1) {
            sb.append("    <h3 style=\"margin-top: 30px; border-bottom: 2px solid #f7efe9; padding-bottom: 8px; color: #047857;\">Plan de Cuotas Actualizado</h3>");
            sb.append("    <table class=\"cuotas-table\">");
            sb.append("      <thead><tr><th>Cuota ID</th><th>Nro.</th><th>Monto</th><th>Vencimiento</th><th>Estado</th></tr></thead>");
            sb.append("      <tbody>");
            try (java.sql.Connection conn = CapaDatos.Conexion.getConexion()) {
                String sqlList = "SELECT id, numero_cuota, monto_cuota, fecha_vencimiento, pagado FROM cuotas WHERE pago_id = ? ORDER BY numero_cuota ASC";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlList)) {
                    ps.setInt(1, pagoId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int cid = rs.getInt("id");
                            int num = rs.getInt("numero_cuota");
                            double val = rs.getDouble("monto_cuota");
                            java.sql.Date venc = rs.getDate("fecha_vencimiento");
                            boolean pag = rs.getBoolean("pagado");
                            
                            sb.append("      <tr>");
                            sb.append("        <td>#").append(cid).append("</td>");
                            sb.append("        <td>").append(num).append("</td>");
                            sb.append("        <td>").append(val).append(" Bs.</td>");
                            sb.append("        <td>").append(venc.toString()).append("</td>");
                            sb.append("        <td>");
                            if (pag) {
                                sb.append("          <span class=\"badge badge-si\">PAGADO</span>");
                            } else {
                                sb.append("          <span class=\"badge badge-pend\">PENDIENTE</span>");
                            }
                            sb.append("        </td>");
                            sb.append("      </tr>");
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("[MailThread] Error al listar cuotas: " + e.getMessage());
            }
            sb.append("      </tbody>");
            sb.append("    </table>");
            sb.append("    <br><p style=\"font-size: 13px; color: #6b7280; text-align: center;\">Recuerda que para pagar tus siguientes cuotas pendientes debes usar el comando <strong>PAGAR_CUOTA[\"CuotaID\"]</strong>.</p>");
        }

        sb.append("  </div>");
        sb.append("  <div class=\"footer\">");
        sb.append("    <strong>Grupo 16 - Tecnología Web (UAGRM)</strong><br>");
        sb.append("    Este es un correo de notificación automática de pagos.");
        sb.append("  </div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }
}

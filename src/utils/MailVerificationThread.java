package utils;

import CapaPresentacion.controllers.RolController;
import CapaPresentacion.controllers.UsuarioController;
import CapaPresentacion.controllers.InsumoController;
import CapaPresentacion.controllers.EnvaseController;
import CapaPresentacion.controllers.ProductoController;
import CapaPresentacion.controllers.PedidoController;
import CapaPresentacion.controllers.CartillaController;
import CapaPresentacion.controllers.ReporteController;
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
    }     /**
     * Se conecta por POP3, descarga los correos, los procesa y los responde.
     */
    private void procesarCorreos() {
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
                System.err.println("[POP3] Conexión fallida. Respuesta inicial del servidor: " + line);
                return;
            }
            System.out.println("[POP3] Conexión establecida.");

            // USER
            System.out.println("[POP3] Iniciando sesión para el usuario: " + user + "...");
            writer.writeBytes(Command.user(user));
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] ERROR en comando USER: " + line);
                return;
            }

            // PASS
            writer.writeBytes(Command.pass(pass));
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] ERROR en contraseña (PASS): " + line);
                return;
            }
            System.out.println("[POP3] Sesión iniciada con éxito.");

            // STAT (obtener total de mensajes y tamaño)
            writer.writeBytes(Command.stat());
            line = reader.readLine();
            if (!line.startsWith("+OK")) {
                System.err.println("[POP3] ERROR en comando STAT: " + line);
                return;
            }

            String[] statParts = line.split(" ");
            int totalMensajes = Integer.parseInt(statParts[1]);

            if (totalMensajes == 0) {
                System.out.println("[POP3] Bandeja vacía. No hay correos nuevos por procesar.");
                writer.writeBytes(Command.quit());
                reader.readLine();
                System.out.println("[POP3] Conexión cerrada.");
                System.out.println("[" + java.time.LocalTime.now().format(dtf) + "] ─── FIN DE LA VERIFICACIÓN ───");
                return;
            }

            System.out.println("[POP3] Bandeja de entrada: " + totalMensajes + " correo(s) por procesar.");

            // Procesar los correos
            for (int i = 1; i <= totalMensajes; i++) {
                System.out.println("\n[POP3] -- Procesando Correo #" + i + " de " + totalMensajes + " --");
                
                // RETR
                writer.writeBytes(Command.retr(i));
                line = reader.readLine();
                if (!line.startsWith("+OK")) {
                    System.out.println("  [ERROR] al recuperar correo #" + i + ": " + line);
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

                System.out.println("  Remitente: " + from);
                System.out.println("  Asunto: " + subject);

                // Ignorar correos de respuesta o reenvío para evitar bucles infinitos
                String subjUpper = subject != null ? subject.toUpperCase().trim() : "";
                if (subjUpper.startsWith("RE:") || subjUpper.startsWith("FWD:") || subjUpper.startsWith("FW:")) {
                    System.out.println("  [POP3] Correo omitido por ser una respuesta/reenvío (evita bucles).");
                    writer.writeBytes(Command.dele(i));
                    reader.readLine(); // Leer confirmación de DELE
                    continue;
                }

                String responseMessage = "";

                // Validar sintaxis y ejecutar
                try {
                    Analex.validarSintaxis(subject);
                    String commandName = Analex.getComando(subject);
                    List<String> params = Analex.getParametros(subject);
                    
                    System.out.println("  Comando detectado: " + commandName + " | Parámetros: " + params);
                    System.out.println("  Ejecutando controlador...");
                    responseMessage = ejecutarComando(commandName, params);
                } catch (IllegalArgumentException e) {
                    System.out.println("  [ERROR DE SINTAXIS] " + e.getMessage());
                    responseMessage = "Error: " + e.getMessage() + "\n\n" +
                                      "Formato esperado:\n" +
                                      "Para comandos con parámetros: COMANDO[\"param1\", \"param2\", ...]\n" +
                                      "Ejemplo: INSPER[\"4715292\", \"Juan Carlos\", ...]\n\n" +
                                      "Para comandos sin parámetros: COMANDO\n" +
                                      "Ejemplo: HELP";
                }

                // Enviar respuesta por SMTP
                System.out.println("  Enviando respuesta vía SMTP a: " + from + "...");
                boolean sent = enviarRespuestaSMTP(from, subject, responseMessage);

                if (sent) {
                    // Si se procesó y respondió bien, marcar para eliminar
                    writer.writeBytes(Command.dele(i));
                    line = reader.readLine();
                    if (line.startsWith("+OK")) {
                        System.out.println("    [POP3] Correo #" + i + " marcado para eliminación.");
                    }
                }
            }

            // QUIT para consolidar eliminaciones
            System.out.println("\n[POP3] Cerrando sesión y consolidando eliminaciones...");
            writer.writeBytes(Command.quit());
            reader.readLine();
            System.out.println("[POP3] Conexión cerrada.");

        } catch (Exception e) {
            System.err.println("\n[MailThread] ERROR GENERAL en ciclo POP3: " + e.getMessage());
        }
        System.out.println("[" + java.time.LocalTime.now().format(dtf) + "] ─── FIN DE LA VERIFICACIÓN ───");
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

        // 3. Recurso: Insumos
        if (InsumoController.canHandle(comando)) {
            return InsumoController.handle(comando, parametros);
        }

        // 4. Recurso: Envases
        if (EnvaseController.canHandle(comando)) {
            return EnvaseController.handle(comando, parametros);
        }

        // 5. Recurso: Productos/Recetas
        if (ProductoController.canHandle(comando)) {
            return ProductoController.handle(comando, parametros);
        }

        // 6. Recurso: Pedidos/Pagos
        if (PedidoController.canHandle(comando)) {
            return PedidoController.handle(comando, parametros);
        }

        // 7. Recurso: Cartilla
        if (CartillaController.canHandle(comando)) {
            return CartillaController.handle(comando, parametros);
        }

        // 8. Recurso: Reportes
        if (ReporteController.canHandle(comando)) {
            return ReporteController.handle(comando, parametros);
        }

        // 9. Ayuda general
        if (comando.equals("HELP") || comando.equals("AYUDA")) {
            return PAyuda.generarHtml();
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
            
            // Cuerpo (Detección dinámica HTML vs Texto Plano)
            if (contenido.trim().startsWith("<!DOCTYPE html>") || contenido.trim().startsWith("<html>")) {
                writer.writeBytes("Content-Type: text/html; charset=UTF-8" + END);
                writer.writeBytes(END); // Separador cabecera-cuerpo
                writer.writeBytes(contenido.replace("\r\n", "\n").replace("\n", "\r\n"));
            } else {
                writer.writeBytes("Content-Type: text/plain; charset=UTF-8" + END);
                writer.writeBytes(END); // Separador cabecera-cuerpo
                writer.writeBytes("--- RESPUESTA AUTOMÁTICA DEL SISTEMA (GRUPO 16) ---" + END + END);
                writer.writeBytes(contenido.replace("\r\n", "\n").replace("\n", "\r\n"));
                writer.writeBytes(END + END + "-------------------------------------------------" + END);
                writer.writeBytes("TecnoEmailZUZU v1.0 - Repostería Automatizada" + END);
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


}

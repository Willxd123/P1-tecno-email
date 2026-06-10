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
        String host = Configuracion.getPopHost();
        int port = Configuracion.getPopPort();
        String user = Configuracion.getPopUser();
        String pass = Configuracion.getPopPassword();

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        String timeStr = java.time.LocalTime.now().format(dtf);

        System.out.println("\n[" + timeStr + "] â”€â”€â”€ INICIANDO VERIFICACIÃ“N DE CORREOS â”€â”€â”€");
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
                writer.writeBytes(contenido.replace("\r\n", "\n").replace("\n", "\r\n"));
            } else {
                writer.writeBytes("Content-Type: text/plain; charset=UTF-8" + END);
                writer.writeBytes(END); // Separador cabecera-cuerpo
                writer.writeBytes("--- RESPUESTA AUTOMÃTICA DEL SISTEMA (GRUPO 16) ---" + END + END);
                writer.writeBytes(contenido.replace("\r\n", "\n").replace("\n", "\r\n"));
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


}


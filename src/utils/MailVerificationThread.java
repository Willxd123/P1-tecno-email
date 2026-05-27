package utils;

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
                System.out.println("[POP3] No hay correos nuevos en la bandeja.");
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

        // 3. Ayuda general
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
            System.out.println("[SMTP-DEBUG] Bienvenida: " + line);
            if (line == null || !line.startsWith("220")) return false;

            // HELO
            writer.writeBytes("HELO mail.tecnoweb.org.bo" + END);
            line = reader.readLine();
            System.out.println("[SMTP-DEBUG] HELO: " + line);
            if (line == null || !line.startsWith("250")) return false;

            // MAIL FROM
            writer.writeBytes("MAIL FROM:<" + remitente + ">" + END);
            line = reader.readLine();
            System.out.println("[SMTP-DEBUG] MAIL FROM: " + line);
            if (line == null || !line.startsWith("250")) return false;

            // RCPT TO
            writer.writeBytes("RCPT TO:<" + destinatario + ">" + END);
            line = reader.readLine();
            System.out.println("[SMTP-DEBUG] RCPT TO: " + line);
            if (line == null || !line.startsWith("250")) return false;

            // DATA
            writer.writeBytes("DATA" + END);
            line = reader.readLine();
            System.out.println("[SMTP-DEBUG] DATA: " + line);
            if (line == null || !line.startsWith("354")) return false;

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
            System.out.println("[SMTP-DEBUG] FIN DATA: " + line);
            if (line == null || !line.startsWith("250")) return false;

            // QUIT
            writer.writeBytes("QUIT" + END);
            line = reader.readLine();
            System.out.println("[SMTP-DEBUG] QUIT: " + line);

            return true;

        } catch (Exception e) {
            System.err.println("[SMTP] ERROR al enviar correo: " + e.getMessage());
            return false;
        }
    }

    private String obtenerAyuda() {
        return "=== COMANDOS DISPONIBLES (GESTIÓN DE USUARIOS - CU1) ===\n\n" +
               "1. Registrar Usuario:\n" +
               "   Asunto: CU1-01[\"Nombre\",\"Apellido\",\"Telefono\",\"Email\",\"Password\",\"Rol\"]\n" +
               "   Roles válidos: Propietario, Secretaria, Cliente\n\n" +
               "2. Editar Usuario:\n" +
               "   Asunto: CU1-02[\"ID\",\"Nombre\",\"Apellido\",\"Telefono\",\"Email\"]\n\n" +
               "3. Cambiar Contraseña:\n" +
               "   Asunto: CU1-03[\"ID\",\"NuevaPassword\"]\n\n" +
               "4. Desactivar Usuario:\n" +
               "   Asunto: CU1-04[\"ID\"]\n\n" +
               "5. Listar Usuarios:\n" +
               "   Asunto: CU1-05\n\n" +
               "6. Buscar Usuario:\n" +
               "   Asunto: CU1-06[\"TextoABuscar\"]\n\n" +
               "7. Ver Perfil Propio:\n" +
               "   Asunto: CU1-07[\"ID\"]\n\n" +
               "=== GESTIÓN DE ROLES (AUXILIAR) ===\n\n" +
               "8. Registrar Rol:\n" +
               "   Asunto: REGROL[\"NombreRol\"]\n\n" +
               "9. Editar Rol:\n" +
               "   Asunto: EDTROL[\"ID\",\"NuevoNombre\"]\n\n" +
               "10. Eliminar Rol:\n" +
               "   Asunto: DELROL[\"ID\"]\n\n" +
               "11. Listar Roles:\n" +
               "   Asunto: LISROL\n\n" +
               "12. Ver Rol:\n" +
               "   Asunto: VERROL[\"ID\"]\n\n" +
               "Nota: Puedes enviar los parámetros con comillas y comas o con corchetes múltiples.";
    }
}

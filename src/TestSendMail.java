import configuracion.Configuracion;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestSendMail {
    private static final String END = "\r\n";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("PROBANDO CONEXION SMTP Y ENVIO DE CORREO");
        System.out.println("==================================================");

        String host = Configuracion.getSmtpHost();
        int port = Configuracion.getSmtpPort();
        String remitente = Configuracion.getSmtpMail();
        
        // Destinatario de prueba: el mismo correo del grupo para verificar la recepción
        String destinatario = "grupo16sc@tecnoweb.org.bo";
        String subject = "Prueba de SMTP pura - Grupo 16";
        String contenido = "Hola! Este es un correo de prueba enviado usando sockets TCP puros desde Java para el Grupo 16.";

        System.out.println("SMTP Host: " + host);
        System.out.println("SMTP Port: " + port);
        System.out.println("Remitente: " + remitente);
        System.out.println("Destinatario: " + destinatario);
        System.out.println("--------------------------------------------------");

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream writer = new DataOutputStream(socket.getOutputStream())) {

            String line;

            // 1. Leer bienvenida del servidor
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("220")) {
                System.err.println("Error: Servidor no disponible o no envió bienvenida 220");
                return;
            }

            // 2. HELO
            String heloCmd = "HELO mail.tecnoweb.org.bo" + END;
            System.out.print("C: " + heloCmd);
            writer.writeBytes(heloCmd);
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("250")) {
                System.err.println("Error en HELO");
                return;
            }

            // 3. MAIL FROM
            String mailFromCmd = "MAIL FROM:<" + remitente + ">" + END;
            System.out.print("C: " + mailFromCmd);
            writer.writeBytes(mailFromCmd);
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("250")) {
                System.err.println("Error en MAIL FROM");
                return;
            }

            // 4. RCPT TO
            String rcptToCmd = "RCPT TO:<" + destinatario + ">" + END;
            System.out.print("C: " + rcptToCmd);
            writer.writeBytes(rcptToCmd);
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("250")) {
                System.err.println("Error en RCPT TO");
                return;
            }

            // 5. DATA
            String dataCmd = "DATA" + END;
            System.out.print("C: " + dataCmd);
            writer.writeBytes(dataCmd);
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("354")) {
                System.err.println("Error en DATA");
                return;
            }

            // 6. Enviar cabeceras y cuerpo
            System.out.println("C: [Enviando Cabeceras y Cuerpo...]");
            writer.writeBytes("From: " + remitente + END);
            writer.writeBytes("To: " + destinatario + END);
            writer.writeBytes("Subject: " + subject + END);
            writer.writeBytes("Content-Type: text/plain; charset=UTF-8" + END);
            writer.writeBytes(END); // Separador cabeceras-cuerpo
            
            writer.writeBytes(contenido + END);
            
            // Fin del correo: punto solo en una línea
            String endData = "." + END;
            System.out.print("C: " + endData);
            writer.writeBytes(endData);
            
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("250")) {
                System.err.println("Error al finalizar DATA");
                return;
            }

            // 7. QUIT
            String quitCmd = "QUIT" + END;
            System.out.print("C: " + quitCmd);
            writer.writeBytes(quitCmd);
            line = reader.readLine();
            System.out.println("S: " + line);

            System.out.println("==================================================");
            System.out.println("PROCESO TERMINADO CON EXITO - CORREO ENVIADO");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("ERROR: Excepción durante la conexión SMTP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

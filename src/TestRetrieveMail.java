import configuracion.Configuracion;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestRetrieveMail {
    private static final String END = "\r\n";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("PROBANDO CONEXION POP3 Y LECTURA DE CORREOS");
        System.out.println("==================================================");

        String host = Configuracion.getPopHost();
        int port = Configuracion.getPopPort();
        String user = Configuracion.getPopUser();
        String pass = Configuracion.getPopPassword();

        System.out.println("POP3 Host: " + host);
        System.out.println("POP3 Port: " + port);
        System.out.println("POP3 User: " + user);
        System.out.println("--------------------------------------------------");

        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream writer = new DataOutputStream(socket.getOutputStream())) {

            String line;

            // 1. Leer bienvenida
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("+OK")) {
                System.err.println("Error en respuesta inicial del servidor");
                return;
            }

            // 2. USER
            String userCmd = "USER " + user + END;
            System.out.print("C: " + userCmd);
            writer.writeBytes(userCmd);
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("+OK")) {
                System.err.println("Error en USER");
                return;
            }

            // 3. PASS
            String passCmd = "PASS " + pass + END;
            System.out.print("C: " + "PASS ********" + END);
            writer.writeBytes(passCmd);
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("+OK")) {
                System.err.println("Error en PASS (Verifica la contraseña del correo en tu .env)");
                return;
            }

            // 4. STAT
            String statCmd = "STAT" + END;
            System.out.print("C: " + statCmd);
            writer.writeBytes(statCmd);
            line = reader.readLine();
            System.out.println("S: " + line);
            if (line == null || !line.startsWith("+OK")) {
                System.err.println("Error en STAT");
                return;
            }

            String[] parts = line.split(" ");
            int totalEmails = Integer.parseInt(parts[1]);
            System.out.println(">>> Total de correos en bandeja: " + totalEmails);

            // 5. LIST
            if (totalEmails > 0) {
                String listCmd = "LIST" + END;
                System.out.print("C: " + listCmd);
                writer.writeBytes(listCmd);
                
                // Leer respuesta de LIST (multilínea terminada en ".")
                line = reader.readLine();
                System.out.println("S: " + line);
                while ((line = reader.readLine()) != null) {
                    if (line.equals(".")) break;
                    System.out.println("   " + line);
                }

                // 6. RETR (recuperar el último correo)
                System.out.println("\n--- Recuperando el último correo (Index " + totalEmails + ") ---");
                String retrCmd = "RETR " + totalEmails + END;
                System.out.print("C: " + retrCmd);
                writer.writeBytes(retrCmd);
                
                line = reader.readLine();
                System.out.println("S: " + line);
                if (line != null && line.startsWith("+OK")) {
                    StringBuilder mailContent = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        if (line.equals(".")) break;
                        mailContent.append(line).append("\n");
                    }
                    System.out.println("---------- CONTENIDO DEL CORREO ----------");
                    System.out.println(mailContent.toString());
                    System.out.println("------------------------------------------");
                }
            }

            // 7. QUIT (Salir ordenadamente sin borrar para que el sistema los pueda procesar después)
            String quitCmd = "QUIT" + END;
            System.out.print("C: " + quitCmd);
            writer.writeBytes(quitCmd);
            line = reader.readLine();
            System.out.println("S: " + line);

            System.out.println("==================================================");
            System.out.println("PROCESO TERMINADO CON EXITO");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("ERROR: Excepcion durante la conexion POP3: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

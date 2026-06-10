import configuracion.Configuracion;
import utils.HiloVerificacionCorreo;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("INICIANDO TECNOEMAILZUZU - GRUPO 16");
        System.out.println("==================================================");

        // Imprimir configuraciones cargadas desde .env de forma centralizada
        System.out.println(">>> Configuracion de Base de Datos:");
        System.out.println("    Host: " + Configuracion.getDbHost() + ":" + Configuracion.getDbPort());
        System.out.println("    BD:   " + Configuracion.getDbName());
        System.out.println("    User: " + Configuracion.getDbUser());

        System.out.println("\n>>> Configuracion de Servidor de Correos (SMTP):");
        System.out.println("    Host: " + Configuracion.getSmtpHost() + ":" + Configuracion.getSmtpPort());
        System.out.println("    Mail: " + Configuracion.getSmtpMail());

        System.out.println("\n>>> Configuracion de Lectura de Correos (POP3):");
        System.out.println("    Host: " + Configuracion.getPopHost() + ":" + Configuracion.getPopPort());
        System.out.println("    User: " + Configuracion.getPopUser());
        System.out.println("==================================================");

        // Iniciar el hilo de lectura y envÃƒÂ­o de correos (POP3/SMTP)
        System.out.println("\n>>> Iniciando Hilo Verificador de Correos...");
        HiloVerificacionCorreo mailThread = new HiloVerificacionCorreo();
        mailThread.start();
    }
}


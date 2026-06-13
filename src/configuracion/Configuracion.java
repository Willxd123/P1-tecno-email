package configuracion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase de Configuración única para el proyecto TecnoEmailZUZU.
 * Permite cargar y consumir las variables de entorno desde el archivo .env de forma centralizada.
 * 
 * Diseñado para ser 100% compatible con Java 8 y posterior, sin dependencias externas.
 */
public class Configuracion {

    private static final Map<String, String> variables = new HashMap<>();

    static {
        cargarVariables();
    }

    /**
     * Carga las variables de entorno desde el archivo .env.
     * Busca en el directorio actual y en subcarpetas en caso de ejecutarse desde el directorio raíz.
     */
    private static void cargarVariables() {
        // Rutas candidatas para el archivo .env
        String[] candidatePaths = {
            ".env",
            "TecnoEmailZUZU/.env",
            "../.env"
        };

        File envFile = null;
        for (String path : candidatePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                envFile = f;
                break;
            }
        }

        if (envFile == null) {
            System.err.println("[Configuracion] ADVERTENCIA: No se pudo encontrar el archivo .env en ninguna ruta conocida.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Ignorar líneas vacías y comentarios
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parsear llave=valor
                int equalIndex = line.indexOf('=');
                if (equalIndex > 0) {
                    String key = line.substring(0, equalIndex).trim();
                    String value = line.substring(equalIndex + 1).trim();

                    // Quitar comillas si el valor está envuelto en ellas
                    if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }

                    variables.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("[Configuracion] ERROR al leer el archivo .env: " + e.getMessage());
        }
    }

    /**
     * Obtiene una variable de entorno por su clave.
     * Si no existe, busca en las variables de entorno del sistema (System.getenv).
     * 
     * @param key Clave de la variable
     * @return El valor correspondiente, o null si no se encuentra
     */
    public static String get(String key) {
        String val = variables.get(key);
        if (val == null) {
            val = System.getenv(key);
        }
        return val;
    }

    /**
     * Obtiene una variable de entorno por su clave con un valor por defecto.
     * 
     * @param key Clave de la variable
     * @param defaultValue Valor de retorno por defecto en caso de no encontrarse
     * @return El valor correspondiente o el valor por defecto
     */
    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Obtiene un entero de las variables de entorno de forma segura.
     */
    private static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                System.err.println("[Configuracion] Error al parsear " + key + " como entero: " + val);
            }
        }
        return defaultValue;
    }

    // ==========================================
    // GETTERS CENTRALIZADOS (DATABASE)
    // ==========================================

    public static String getDbUser() {
        return get("DB_USER", "grupo16sc");
    }

    public static String getDbPassword() {
        return get("DB_PASSWORD", "grup016grup016");
    }

    public static String getDbHost() {
        return get("DB_HOST", "tecnoweb.org.bo");
    }

    public static int getDbPort() {
        return getInt("DB_PORT", 5432);
    }

    public static String getDbName() {
        return get("DB_NAME", "db_grupo16sc");
    }

    // ==========================================
    // GETTERS CENTRALIZADOS (SMTP - SEND EMAIL)
    // ==========================================

    public static String getSmtpHost() {
        return get("SMTP_HOST", "mail.tecnoweb.org.bo");
    }

    public static int getSmtpPort() {
        return getInt("SMTP_PORT", 25);
    }

    public static String getSmtpProtocol() {
        return get("SMTP_PROTOCOL", "smtp");
    }

    public static String getSmtpUser() {
        return get("SMTP_USER", "grupo16sc");
    }

    public static String getSmtpMail() {
        return get("SMTP_MAIL", "grupo16sc@tecnoweb.org.bo");
    }

    public static String getSmtpPassword() {
        return get("SMTP_PASSWORD", "grup016grup016");
    }

    // ==========================================
    // GETTERS CENTRALIZADOS (POP - RECIEVE EMAIL)
    // ==========================================

    public static String getPopHost() {
        return get("POP_HOST", "mail.tecnoweb.org.bo");
    }

    public static int getPopPort() {
        return getInt("POP_PORT", 110);
    }

    public static String getPopUser() {
        return get("POP_USER", "grupo16sc");
    }

    public static String getPopPassword() {
        return get("POP_PASSWORD", "grup016grup016");
    }

    // ==========================================
    // GETTERS CENTRALIZADOS (PAGOFACIL INTEGRATION)
    // ==========================================

    public static String getPagoFacilCommerceId() {
        return get("PAGOFACIL_COMMERCE_ID");
    }

    public static String getPagoFacilTokenService() {
        return get("PAGOFACIL_TOKEN_SERVICE");
    }

    public static String getPagoFacilTokenSecret() {
        return get("PAGOFACIL_TOKEN_SECRET");
    }
}

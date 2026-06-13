package utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import configuracion.Configuracion;

/**
 * Cliente de Integración de PagoFacil QR Master API.
 * Permite autenticarse, generar códigos QR de pago y consultar el estado de las transacciones.
 */
public class PagoFacilService {

    private static final String BASE_URL = "https://masterqr.pagofacil.com.bo/api/services/v2";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Hace login en PagoFacil y devuelve el Token de Acceso JWT (accessToken).
     */
    public static String login() {
        String tokenService = Configuracion.getPagoFacilTokenService();
        String tokenSecret = Configuracion.getPagoFacilTokenSecret();

        if (tokenService == null || tokenSecret == null) {
            System.err.println("[PagoFacil] Error: Credenciales no configuradas en el archivo .env");
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("tcTokenService", tokenService)
                    .header("tcTokenSecret", tokenSecret)
                    .POST(BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseJsonField(response.body(), "accessToken");
            } else {
                System.err.println("[PagoFacil] Error en login. Status: " + response.statusCode() + " | Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[PagoFacil] Excepción en login: " + e.getMessage());
        }
        return null;
    }

    /**
     * Genera un código QR de pago y devuelve el string Base64 de la imagen del QR.
     * 
     * @param clientName Nombre del cliente
     * @param clientPhone Teléfono del cliente
     * @param clientEmail Correo del cliente
     * @param companyTxId ID único de transacción de nuestra empresa (ej: PED-1008 o CUO-3005)
     * @param amount Monto a cobrar en Bs.
     * @param itemDescription Descripción corta de los productos
     * @return El string Base64 del código QR, o null si falla.
     */
    public static String generarQR(String clientName, String clientPhone, String clientEmail, String companyTxId, double amount, String itemDescription) {
        String token = login();
        if (token == null) {
            return null;
        }

        // Aseguramos valores por defecto si son nulos
        if (clientName == null || clientName.trim().isEmpty()) clientName = "Cliente Reposteria";
        if (clientPhone == null || clientPhone.trim().isEmpty()) clientPhone = "70000000";
        if (clientEmail == null || clientEmail.trim().isEmpty()) clientEmail = "cliente@correo.com";
        if (itemDescription == null || itemDescription.trim().isEmpty()) itemDescription = "Pedido Reposteria Zuzu";

        // Escapar caracteres para JSON simple
        clientName = escapeJson(clientName);
        itemDescription = escapeJson(itemDescription);

        // Construir JSON body - Hardcodeamos 0.1 para pruebas reales de bajo costo
        String jsonBody = "{\n" +
                "    \"paymentMethod\": 34,\n" +
                "    \"clientName\": \"" + clientName + "\",\n" +
                "    \"documentType\": 1,\n" +
                "    \"documentId\": \"" + clientPhone + "\",\n" + // Usamos teléfono como CI
                "    \"phoneNumber\": \"" + clientPhone + "\",\n" +
                "    \"email\": \"" + clientEmail + "\",\n" +
                "    \"paymentNumber\": \"" + companyTxId + "\",\n" +
                "    \"amount\": 0.1,\n" +
                "    \"currency\": 2,\n" + // 2: BOB (Bolivianos)
                "    \"clientCode\": \"" + companyTxId + "\",\n" +
                "    \"callbackUrl\": \"https://masterqr.pagofacil.com.bo/api/services/v2/callback-dummy\",\n" +
                "    \"orderDetail\": [\n" +
                "        {\n" +
                "            \"serial\": 1,\n" +
                "            \"product\": \"" + itemDescription + "\",\n" +
                "            \"quantity\": 1,\n" +
                "            \"price\": 0.1,\n" +
                "            \"discount\": 0,\n" +
                "            \"total\": 0.1\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/generate-qr"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String rawQr = parseJsonField(response.body(), "qrBase64");
                if (rawQr != null) {
                    String cleanQr = rawQr.replace("\\/", "/");
                    return formatBase64(cleanQr);
                }
            } else {
                System.err.println("[PagoFacil] Error al generar QR. Status: " + response.statusCode() + " | Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[PagoFacil] Excepción al generar QR: " + e.getMessage());
        }
        return null;
    }

    /**
     * Consulta el estado de pago de una transacción en PagoFacil por su ID de empresa.
     * 
     * @param companyTxId ID de transacción de nuestra empresa (ej: PED-1008 o CUO-3005)
     * @return true si la transacción ya está pagada en PagoFacil, false en caso contrario.
     */
    public static boolean consultarEstado(String companyTxId) {
        String token = login();
        if (token == null) {
            return false;
        }

        String jsonBody = "{\n" +
                "    \"companyTransactionId\": \"" + companyTxId + "\"\n" +
                "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/query-transaction"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                String paymentStatus = parseJsonField(body, "paymentStatus");
                
                // Retorna true si el estado es "Pagado" o similar
                return "Pagado".equalsIgnoreCase(paymentStatus) 
                    || "Paid".equalsIgnoreCase(paymentStatus) 
                    || "1".equals(paymentStatus)
                    || "Success".equalsIgnoreCase(paymentStatus)
                    || "Completed".equalsIgnoreCase(paymentStatus);
            } else {
                System.err.println("[PagoFacil] Error al consultar estado. Status: " + response.statusCode() + " | Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("[PagoFacil] Excepción al consultar estado: " + e.getMessage());
        }
        return false;
    }

    /**
     * Utilidad simple para parsear un campo String de un JSON plano usando Expresiones Regulares.
     */
    private static String parseJsonField(String json, String field) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + field + "\"[\\s:]*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }

    private static final String TX_FILE_PATH = "qr_transactions.json";

    public static synchronized java.util.Map<String, String> cargarTransacciones() {
        java.util.Map<String, String> map = new java.util.HashMap<>();
        java.io.File file = new java.io.File(TX_FILE_PATH);
        if (!file.exists()) {
            return map;
        }
        try {
            String content = java.nio.file.Files.readString(file.toPath());
            Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                map.put(matcher.group(1), matcher.group(2));
            }
        } catch (Exception e) {
            System.err.println("[PagoFacilService] Error al cargar transacciones: " + e.getMessage());
        }
        return map;
    }

    public static synchronized void guardarTransacciones(java.util.Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int size = map.size();
        int count = 0;
        for (java.util.Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
            count++;
            if (count < size) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("}");
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of(TX_FILE_PATH), sb.toString());
        } catch (Exception e) {
            System.err.println("[PagoFacilService] Error al guardar transacciones: " + e.getMessage());
        }
    }

    public static void registrarTransaccion(String txId, String email, double monto, String tipo) {
        java.util.Map<String, String> map = cargarTransacciones();
        map.put(txId, email + ";" + monto + ";" + tipo);
        guardarTransacciones(map);
        System.out.println("[PagoFacilService] Transacción registrada en JSON local: " + txId + " (" + tipo + ")");
    }

    public static void removerTransaccion(String txId) {
        java.util.Map<String, String> map = cargarTransacciones();
        if (map.remove(txId) != null) {
            guardarTransacciones(map);
            System.out.println("[PagoFacilService] Transacción removida de JSON local: " + txId);
        }
    }

    private static String formatBase64(String base64) {
        if (base64 == null) return null;
        StringBuilder sb = new StringBuilder();
        int len = base64.length();
        for (int i = 0; i < len; i += 76) {
            int end = Math.min(i + 76, len);
            sb.append(base64, i, end).append("\r\n");
        }
        return sb.toString();
    }
}

package CapaPresentacion;

import java.util.List;

public class PRoles {

    /**
     * Convierte el resultado plano del negocio de Roles en una vista HTML espectacular.
     */
    public static String generarHtml(String comando, String resultado) {
        String tituloModulo = "Gestión de Roles - Repostería ZUZU";
        StringBuilder bodyHtml = new StringBuilder();

        boolean esError = resultado.trim().toLowerCase().startsWith("error");

        // Cabecera del Contenido
        bodyHtml.append("<h2 class=\"card-title\">Comando Procesado: ").append(comando).append("</h2>");

        if (resultado.contains("|")) {
            // Es un listado en formato tabla ASCII, lo parseamos a una tabla HTML premium
            bodyHtml.append(parsearTablaAscii(resultado));
        } else {
            // Es un mensaje de éxito o de error
            String alertClass = esError ? "alert-error" : "alert-success";
            String alertTitle = esError ? "⚠️ OCURRIÓ UN INCONVENIENTE" : "🎉 OPERACIÓN EXITOSA";
            
            bodyHtml.append("<div class=\"alert ").append(alertClass).append("\">")
                    .append("<strong>").append(alertTitle).append("</strong><br>")
                    .append(resultado.replace("\n", "<br>"))
                    .append("</div>");
        }

        return construirPlantillaBase(tituloModulo, bodyHtml.toString());
    }

    /**
     * Parsea una tabla de texto ASCII de NRoles a una tabla HTML estilizada.
     */
    private static String parsearTablaAscii(String asciiText) {
        String[] lineas = asciiText.split("\n");
        if (lineas.length == 0) {
            return "<p>No se encontraron datos.</p>";
        }

        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<table>");

        boolean esCabecera = true;

        for (String linea : lineas) {
            linea = linea.trim();
            // Ignorar líneas separadoras (como -------) o vacías
            if (linea.isEmpty() || linea.startsWith("---") || linea.startsWith("===") || linea.startsWith("Resultados")) {
                continue;
            }

            if (!linea.contains("|")) {
                // Agregar texto suelto como párrafo
                htmlTable.append("</table><p style=\"font-style: italic; margin-top: 10px;\">")
                         .append(linea)
                         .append("</p><table>");
                continue;
            }

            String[] celdas = linea.split("\\|");
            htmlTable.append("<tr>");

            for (int col = 0; col < celdas.length; col++) {
                String celdaText = celdas[col].trim();

                if (esCabecera) {
                    htmlTable.append("<th>").append(celdaText).append("</th>");
                } else {
                    htmlTable.append("<td>");
                    // Aplicar badges estilizados según el contenido
                    if (col == 1) {
                        htmlTable.append("<span class=\"badge badge-rol\">").append(celdaText).append("</span>");
                    } else {
                        htmlTable.append(celdaText);
                    }
                    htmlTable.append("</td>");
                }
            }

            htmlTable.append("</tr>");
            esCabecera = false;
        }

        htmlTable.append("</table>");
        return htmlTable.toString();
    }

    /**
     * Plantilla base de diseño premium de repostería con colores HSL caramel/chocolate.
     */
    private static String construirPlantillaBase(String titulo, String contenido) {
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "  <meta charset=\"utf-8\">\n" +
               "  <style>\n" +
               "    body {\n" +
               "      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;\n" +
               "      background-color: #fdfaf7;\n" +
               "      color: #4a3e3d;\n" +
               "      margin: 0;\n" +
               "      padding: 0;\n" +
               "    }\n" +
               "    .container {\n" +
               "      max-width: 650px;\n" +
               "      margin: 40px auto;\n" +
               "      background-color: #ffffff;\n" +
               "      border-radius: 20px;\n" +
               "      overflow: hidden;\n" +
               "      box-shadow: 0 10px 40px rgba(97, 56, 28, 0.08);\n" +
               "      border: 1px solid #f0e6df;\n" +
               "    }\n" +
               "    .header {\n" +
               "      background: linear-gradient(135deg, #61381c, #8b5a2b);\n" +
               "      padding: 35px 20px;\n" +
               "      text-align: center;\n" +
               "      color: #ffffff;\n" +
               "    }\n" +
               "    .header h1 {\n" +
               "      margin: 0;\n" +
               "      font-size: 26px;\n" +
               "      font-weight: 700;\n" +
               "      letter-spacing: 0.8px;\n" +
               "      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);\n" +
               "    }\n" +
               "    .header p {\n" +
               "      margin: 8px 0 0 0;\n" +
               "      font-size: 14px;\n" +
               "      opacity: 0.9;\n" +
               "    }\n" +
               "    .content {\n" +
               "      padding: 35px 30px;\n" +
               "    }\n" +
               "    .card-title {\n" +
               "      font-size: 19px;\n" +
               "      font-weight: 600;\n" +
               "      margin-top: 0;\n" +
               "      margin-bottom: 20px;\n" +
               "      color: #61381c;\n" +
               "      border-bottom: 2px solid #f7efe9;\n" +
               "      padding-bottom: 10px;\n" +
               "    }\n" +
               "    .alert {\n" +
               "      padding: 18px;\n" +
               "      border-radius: 14px;\n" +
               "      margin-bottom: 25px;\n" +
               "      font-size: 14px;\n" +
               "      line-height: 1.6;\n" +
               "    }\n" +
               "    .alert-success {\n" +
               "      background-color: #f0fdf4;\n" +
               "      border: 1px solid #bbf7d0;\n" +
               "      color: #166534;\n" +
               "    }\n" +
               "    .alert-error {\n" +
               "      background-color: #fef2f2;\n" +
               "      border: 1px solid #fecaca;\n" +
               "      color: #991b1b;\n" +
               "    }\n" +
               "    table {\n" +
               "      width: 100%;\n" +
               "      border-collapse: separate;\n" +
               "      border-spacing: 0;\n" +
               "      margin-top: 15px;\n" +
               "      border-radius: 12px;\n" +
               "      overflow: hidden;\n" +
               "      border: 1px solid #eedfd4;\n" +
               "    }\n" +
               "    th {\n" +
               "      background-color: #8b5a2b;\n" +
               "      color: #ffffff;\n" +
               "      font-weight: 600;\n" +
               "      text-align: left;\n" +
               "      padding: 14px 16px;\n" +
               "      font-size: 13px;\n" +
               "      letter-spacing: 0.3px;\n" +
               "    }\n" +
               "    td {\n" +
               "      padding: 14px 16px;\n" +
               "      border-bottom: 1px solid #f7efe9;\n" +
               "      font-size: 13px;\n" +
               "      color: #4a3e3d;\n" +
               "    }\n" +
               "    tr:last-child td {\n" +
               "      border-bottom: none;\n" +
               "    }\n" +
               "    tr:nth-child(even) {\n" +
               "      background-color: #fdfbf9;\n" +
               "    }\n" +
               "    .badge {\n" +
               "      display: inline-block;\n" +
               "      padding: 4px 10px;\n" +
               "      border-radius: 30px;\n" +
               "      font-size: 11px;\n" +
               "      font-weight: 600;\n" +
               "      text-align: center;\n" +
               "    }\n" +
               "    .badge-rol {\n" +
               "      background-color: #fef3c7;\n" +
               "      color: #d97706;\n" +
               "      border: 1px solid #fde68a;\n" +
               "    }\n" +
               "    .footer {\n" +
               "      background-color: #fcf8f5;\n" +
               "      padding: 25px;\n" +
               "      text-align: center;\n" +
               "      font-size: 12px;\n" +
               "      color: #8c7b70;\n" +
               "      border-top: 1px solid #f0e6df;\n" +
               "    }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"container\">\n" +
               "    <div class=\"header\">\n" +
               "      <h1>🍰 REPOSTERÍA ZUZU 🍰</h1>\n" +
               "      <p>Sistema Automatizado por Correo Electrónico</p>\n" +
               "    </div>\n" +
               "    <div class=\"content\">\n" +
               "      " + contenido + "\n" +
               "    </div>\n" +
               "    <div class=\"footer\">\n" +
               "      <strong>Grupo 16 - Tecnología Web (UAGRM)</strong><br>\n" +
               "      Este es un correo automático, por favor no lo respondas directamente.\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }
}

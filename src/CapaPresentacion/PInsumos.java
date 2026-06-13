package CapaPresentacion;

public class PInsumos {

    public static String generarHtml(String comando, String resultado) {
        String tituloModulo = "Gestión de Insumos - “CHIFONES PERUANOS ZUZÚ”";
        StringBuilder bodyHtml = new StringBuilder();

        boolean esError = resultado.trim().toLowerCase().startsWith("error");

        bodyHtml.append("<h2 class=\"card-title\">Comando Procesado: ").append(comando).append("</h2>");

        if (resultado.contains("|")) {
            // Tabla ASCII a HTML
            bodyHtml.append(parsearTablaAscii(resultado));
        } else {
            // Mensaje simple
            String alertClass = esError ? "alert-error" : "alert-success";
            String alertTitle = esError ? "ADVERTENCIA / ERROR" : "OPERACIÓN EXITOSA";
            
            bodyHtml.append("<div class=\"alert ").append(alertClass).append("\">")
                    .append("<strong>").append(alertTitle).append("</strong><br>")
                    .append(resultado.replace("\n", "<br>"))
                    .append("</div>");
        }

        return construirPlantillaBase(tituloModulo, bodyHtml.toString());
    }

    private static String parsearTablaAscii(String asciiText) {
        String[] lineas = asciiText.split("\n");
        if (lineas.length == 0) {
            return "<p>No hay datos disponibles.</p>";
        }

        StringBuilder htmlTable = new StringBuilder();
        
        // Si la primera línea tiene el título del historial
        int startIdx = 0;
        if (!lineas[0].contains("|") && lineas[0].length() > 0) {
            htmlTable.append("<h3 style=\"color: #61381c; margin-bottom: 10px;\">").append(lineas[0]).append("</h3>");
            startIdx = 1;
        }

        htmlTable.append("<table>");
        boolean esCabecera = true;

        for (int i = startIdx; i < lineas.length; i++) {
            String linea = lineas[i].trim();
            if (linea.isEmpty() || linea.startsWith("---") || linea.startsWith("===")) {
                continue;
            }

            if (!linea.contains("|")) {
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
                    // Estilo de stock crítico o valores especiales
                    if (col == 3) {
                        try {
                            double val = Double.parseDouble(celdaText);
                            htmlTable.append("<strong style=\"color: #2b2b2b;\">").append(celdaText).append("</strong>");
                        } catch (NumberFormatException e) {
                            htmlTable.append(celdaText);
                        }
                    } else if (celdaText.startsWith("-")) {
                        // Consumo o merma (número negativo) en rojo
                        htmlTable.append("<span style=\"color: #b91c1c; font-weight: bold;\">").append(celdaText).append("</span>");
                    } else if (celdaText.equalsIgnoreCase("entrada") || celdaText.equalsIgnoreCase("ajuste")) {
                        htmlTable.append("<span class=\"badge badge-si\">").append(celdaText.toUpperCase()).append("</span>");
                    } else if (celdaText.equalsIgnoreCase("merma") || celdaText.equalsIgnoreCase("consumo")) {
                        htmlTable.append("<span class=\"badge badge-no\">").append(celdaText.toUpperCase()).append("</span>");
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
               "      background: linear-gradient(135deg, #8b5a2b, #b27a42);\n" +
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
               "      color: #8b5a2b;\n" +
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
               "    .badge-si {\n" +
               "      background-color: #dcfce7;\n" +
               "      color: #15803d;\n" +
               "      border: 1px solid #bbf7d0;\n" +
               "    }\n" +
               "    .badge-no {\n" +
               "      background-color: #fee2e2;\n" +
               "      color: #b91c1c;\n" +
               "      border: 1px solid #fca5a5;\n" +
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
               "      <img src=\"https://i.ibb.co/RpQ8WGhK/bienvenida.png\" alt=\"Chifones Peruanos Zuzú Logo\" style=\"max-height: 80px; margin-bottom: 12px; display: block; margin-left: auto; margin-right: auto;\">\n" +
               "      <h1>INVENTARIO DE INSUMOS</h1>\n" +
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

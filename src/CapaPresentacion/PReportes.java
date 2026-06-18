package CapaPresentacion;

public class PReportes {

    public static String generarHtml(String comando, String resultado) {
        String tituloModulo = "Reportes Estadísticos y Gerenciales - “CHIFONES PERUANOS ZUZÚ”";
        StringBuilder bodyHtml = new StringBuilder();

        boolean esError = resultado.trim().toLowerCase().startsWith("error");

        bodyHtml.append("<h2 class=\"card-title\">Reporte Solicitado: ").append(comando).append("</h2>");

        if (resultado.contains("|")) {
            bodyHtml.append(parsearTablaAscii(comando, resultado));
        } else {
            String alertClass = esError ? "alert-error" : "alert-success";
            String alertTitle = esError ? "ADVERTENCIA / ERROR" : "INFORMACIÓN";
            
            bodyHtml.append("<div class=\"alert ").append(alertClass).append("\">")
                    .append("<strong>").append(alertTitle).append("</strong><br>")
                    .append(resultado.replace("\n", "<br>"))
                    .append("</div>");
        }

        return construirPlantillaBase(tituloModulo, bodyHtml.toString());
    }

    private static String parsearTablaAscii(String comando, String asciiText) {
        String[] lineas = asciiText.split("\n");
        if (lineas.length == 0) {
            return "<p>No hay datos disponibles.</p>";
        }

        StringBuilder htmlTable = new StringBuilder();
        int startIdx = 0;

        // Si la primera línea tiene el título del detalle/historial
        if (!lineas[0].contains("|") && lineas[0].length() > 0) {
            htmlTable.append("<h3 style=\"color: #1e3a8a; margin-bottom: 10px;\">").append(lineas[0]).append("</h3>");
            startIdx = 1;
            // Si la segunda línea también es informativa (ej: totales del reporte de ventas)
            if (lineas.length > 1 && !lineas[1].contains("|") && !lineas[1].startsWith("---") && lineas[1].length() > 0) {
                htmlTable.append("<div style=\"margin: 15px 0; padding: 15px; background-color: #eff6ff; border-radius: 12px; border: 1px solid #bfdbfe; font-size: 14px; color: #1e40af;\">")
                          .append("<strong>Resumen Ejecutivo:</strong><br>")
                          .append(lineas[1].replace(" | ", "<br>"))
                          .append("</div>");
                startIdx = 2;
            }
        }

        htmlTable.append("<table>");
        boolean esCabecera = true;

        for (int i = startIdx; i < lineas.length; i++) {
            String linea = lineas[i].trim();
            if (linea.isEmpty() || linea.startsWith("---") || linea.startsWith("===")) {
                continue;
            }

            if (!linea.contains("|")) {
                // Si es la línea de resumen de totales
                htmlTable.append("</table>");
                htmlTable.append("<div style=\"margin-top: 20px; padding: 15px; background-color: #f0fdf4; border-radius: 12px; border: 1px solid #bbf7d0; font-size: 15px; text-align: right;\">")
                          .append("<strong style=\"color: #166534;\">").append(linea).append("</strong>")
                          .append("</div>");
                htmlTable.append("<table>");
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
                    // Resaltar alertas de stock critico o cuotas
                    if (celdaText.matches("^\\d+(\\.\\d+)?$") && col == 2 && comando.equals("CU8-06")) {
                        // Stock actual bajo
                        htmlTable.append("<span style=\"color: #b91c1c; font-weight: 600;\">").append(celdaText).append("</span>");
                    } else if (celdaText.equalsIgnoreCase("CONTADO") || celdaText.equalsIgnoreCase("DEVUELTO")) {
                        htmlTable.append("<span class=\"badge badge-si\">").append(celdaText.toUpperCase()).append("</span>");
                    } else if (celdaText.equalsIgnoreCase("CUOTAS") || celdaText.equalsIgnoreCase("CREDITO")) {
                        htmlTable.append("<span class=\"badge badge-pend\">").append(celdaText.toUpperCase()).append("</span>");
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
               "      background-color: #f6f8fa;\n" +
               "      color: #334155;\n" +
               "      margin: 0;\n" +
               "      padding: 0;\n" +
               "    }\n" +
               "    .container {\n" +
               "      max-width: 750px;\n" +
               "      margin: 40px auto;\n" +
               "      background-color: #ffffff;\n" +
               "      border-radius: 20px;\n" +
               "      overflow: hidden;\n" +
               "      box-shadow: 0 10px 30px rgba(30, 41, 59, 0.05);\n" +
               "      border: 1px solid #e2e8f0;\n" +
               "    }\n" +
               "    .header {\n" +
               "      background: linear-gradient(135deg, #1e3a8a, #0f172a);\n" +
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
               "      color: #1e3a8a;\n" +
               "      border-bottom: 2px solid #e2e8f0;\n" +
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
               "      background-color: #eff6ff;\n" +
               "      border: 1px solid #bfdbfe;\n" +
               "      color: #1e40af;\n" +
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
               "      border: 1px solid #cbd5e1;\n" +
               "    }\n" +
               "    th {\n" +
               "      background-color: #1e3a8a;\n" +
               "      color: #ffffff;\n" +
               "      font-weight: 600;\n" +
               "      text-align: left;\n" +
               "      padding: 14px 16px;\n" +
               "      font-size: 13px;\n" +
               "      letter-spacing: 0.3px;\n" +
               "    }\n" +
               "    td {\n" +
               "      padding: 14px 16px;\n" +
               "      border-bottom: 1px solid #e2e8f0;\n" +
               "      font-size: 13px;\n" +
               "      color: #334155;\n" +
               "    }\n" +
               "    tr:last-child td {\n" +
               "      border-bottom: none;\n" +
               "    }\n" +
               "    tr:nth-child(even) {\n" +
               "      background-color: #f8fafc;\n" +
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
               "    .badge-pend {\n" +
               "      background-color: #fef3c7;\n" +
               "      color: #d97706;\n" +
               "      border: 1px solid #fde68a;\n" +
               "    }\n" +
               "    .badge-no {\n" +
               "      background-color: #fee2e2;\n" +
               "      color: #b91c1c;\n" +
               "      border: 1px solid #fca5a5;\n" +
               "    }\n" +
               "    .footer {\n" +
               "      background-color: #f8fafc;\n" +
               "      padding: 25px;\n" +
               "      text-align: center;\n" +
               "      font-size: 12px;\n" +
               "      color: #64748b;\n" +
               "      border-top: 1px solid #e2e8f0;\n" +
               "    }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"container\">\n" +
               "    <div class=\"header\">\n" +
               "      <img src=\"https://i.ibb.co/RpQ8WGhK/bienvenida.png\" alt=\"Chifones Peruanos Zuzú Logo\" style=\"max-height: 80px; margin-bottom: 12px; display: block; margin-left: auto; margin-right: auto;\">\n" +
               "      <h1>REPORTE DE GESTIÓN GERENCIAL</h1>\n" +
               "      <p>Módulo de Inteligencia de Negocio y Estadísticas</p>\n" +
               "    </div>\n" +
               "    <div class=\"content\">\n" +
               "      " + contenido + "\n" +
               "    </div>\n" +
               "    <div class=\"footer\">\n" +
               "      <strong>Grupo 16 - Tecnología Web (UAGRM)</strong><br>\n" +
               "      Este es un correo electrónico confidencial para el propietario del sistema.\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }
}

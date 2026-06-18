package CapaPresentacion;

public class PCartillas {

    public static String generarHtml(String comando, String resultado) {
        String tituloModulo = "Cartilla Digital del Cliente - “CHIFONES PERUANOS ZUZU”";
        StringBuilder bodyHtml = new StringBuilder();

        boolean esError = resultado.trim().toLowerCase().startsWith("error");

        bodyHtml.append("<h2 class=\"card-title\">Consulta de Cartilla: ").append(comando).append("</h2>");

        if (resultado.contains("CARTILLA ID:")) {
            bodyHtml.append(parsearEstructuraCartillas(resultado));
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

    private static String parsearEstructuraCartillas(String text) {
        String[] bloques = text.split("==========================================\n");
        if (bloques.length == 0) {
            return "<p>No hay datos disponibles.</p>";
        }

        StringBuilder html = new StringBuilder();

        // El primer bloque es el encabezado del cliente
        String clienteHeader = bloques[0].trim();
        if (clienteHeader.startsWith("CLIENTE:")) {
            html.append("<div class=\"client-banner\">")
                .append("<h3>").append(clienteHeader).append("</h3>")
                .append("</div>");
        }

        // Los siguientes bloques son las cartillas
        for (int b = 1; b < bloques.length; b++) {
            String bloque = bloques[b].trim();
            if (bloque.isEmpty()) continue;

            String[] lineas = bloque.split("\n");
            
            String cartillaId = "";
            String estado = "";
            String fechaInicio = "";
            String fechaFin = "";
            String fechaCanje = "";
            String premioSabor = "";
            String premioEnvase = "";
            int chifonesNum = 0;
            int envasesNum = 0;
            
            StringBuilder pedidosHtml = new StringBuilder();
            StringBuilder envasesHtml = new StringBuilder();
            
            int section = 0; // 0: datos basicos, 1: pedidos, 2: envases

            for (String linea : lineas) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (linea.startsWith("--- PEDIDOS EN ESTA CARTILLA ---")) {
                    section = 1;
                    continue;
                } else if (linea.startsWith("--- CONTROL ENVS EN ESTA CARTILLA ---")) {
                    section = 2;
                    continue;
                }

                if (section == 0) {
                    if (linea.startsWith("CARTILLA ID:")) cartillaId = linea.replace("CARTILLA ID:", "").trim();
                    else if (linea.startsWith("ESTADO:")) estado = linea.replace("ESTADO:", "").trim();
                    else if (linea.startsWith("FECHA INICIO:")) fechaInicio = linea.replace("FECHA INICIO:", "").trim();
                    else if (linea.startsWith("FECHA FIN:")) fechaFin = linea.replace("FECHA FIN:", "").trim();
                    else if (linea.startsWith("FECHA CANJE:")) fechaCanje = linea.replace("FECHA CANJE:", "").trim();
                    else if (linea.startsWith("PREMIO SABOR:")) premioSabor = linea.replace("PREMIO SABOR:", "").trim();
                    else if (linea.startsWith("PREMIO ENVASE DEVUELTO:")) premioEnvase = linea.replace("PREMIO ENVASE DEVUELTO:", "").trim();
                    else if (linea.startsWith("ACUMULADO CHIFONES:")) {
                        String raw = linea.replace("ACUMULADO CHIFONES:", "").trim();
                        try { chifonesNum = Integer.parseInt(raw.split("/")[0]); } catch (Exception e) {}
                    }
                    else if (linea.startsWith("ACUMULADO ENVASES:")) {
                        String raw = linea.replace("ACUMULADO ENVASES:", "").trim();
                        try { envasesNum = Integer.parseInt(raw.split("/")[0]); } catch (Exception e) {}
                    }
                } else if (section == 1) {
                    if (linea.startsWith("PEDIDO:")) {
                        String raw = linea.replace("PEDIDO:", "").trim();
                        String[] partes = raw.split("\\|");
                        if (partes.length >= 4) {
                            pedidosHtml.append("<tr>")
                                       .append("<td>#").append(partes[0].trim()).append("</td>")
                                       .append("<td>").append(partes[1].trim()).append("</td>")
                                       .append("<td>").append(partes[2].trim()).append("</td>")
                                       .append("<td><span class=\"badge badge-")
                                       .append(partes[3].trim().equalsIgnoreCase("PAGADO") || partes[3].trim().equalsIgnoreCase("ENTREGADO") ? "si" : "pend")
                                       .append("\">").append(partes[3].trim().toUpperCase()).append("</span></td>")
                                       .append("<td>").append(partes.length > 4 ? partes[4].trim() : "").append("</td>")
                                       .append("</tr>");
                        }
                    } else if (linea.equalsIgnoreCase("Ninguno")) {
                        pedidosHtml.append("<tr><td colspan=\"5\" style=\"text-align:center;color:#9ca3af;\">Ningún pedido en esta cartilla.</td></tr>");
                    }
                } else if (section == 2) {
                    if (linea.startsWith("ENVASE:")) {
                        String raw = linea.replace("ENVASE:", "").trim();
                        String[] partes = raw.split("\\|");
                        if (partes.length >= 5) {
                            String tipo = partes[0].trim();
                            String prestados = partes[1].replace("Prestados:", "").trim();
                            String devueltos = partes[2].replace("Devueltos:", "").trim();
                            String fechaDev = partes[3].replace("Fecha Dev:", "").trim();
                            String estEnv = partes[4].replace("Estado:", "").trim();
                            
                            String bClass = "no";
                            if (estEnv.equalsIgnoreCase("DEVUELTO")) bClass = "si";
                            else if (estEnv.equalsIgnoreCase("PARCIAL")) bClass = "pend";

                            envasesHtml.append("<tr>")
                                       .append("<td>").append(tipo).append("</td>")
                                       .append("<td style=\"text-align:center;\">").append(prestados).append("</td>")
                                       .append("<td style=\"text-align:center;\">").append(devueltos).append("</td>")
                                       .append("<td>").append(fechaDev).append("</td>")
                                       .append("<td><span class=\"badge badge-").append(bClass).append("\">").append(estEnv.toUpperCase()).append("</span></td>")
                                       .append("</tr>");
                        }
                    } else if (linea.equalsIgnoreCase("Ninguno")) {
                        envasesHtml.append("<tr><td colspan=\"5\" style=\"text-align:center;color:#9ca3af;\">Ningún envase prestado en esta cartilla.</td></tr>");
                    }
                }
            }

            // Construir la tarjeta de la cartilla
            String cardHeaderClass = "cartilla-activa";
            String badgeText = "ACTIVA - EN PROGRESO";
            if (estado.equalsIgnoreCase("completada")) {
                cardHeaderClass = "cartilla-completada";
                badgeText = "¡COMPLETADA! LISTA PARA CANJEAR";
            } else if (estado.equalsIgnoreCase("canjeada")) {
                cardHeaderClass = "cartilla-canjeada";
                badgeText = "CANJEADA E HISTÓRICA";
            }

            html.append("<div class=\"cartilla-card\">")
                .append("  <div class=\"cartilla-header ").append(cardHeaderClass).append("\">")
                .append("    <span class=\"cartilla-title\">Cartilla #").append(cartillaId).append("</span>")
                .append("    <span class=\"cartilla-badge\">").append(badgeText).append("</span>")
                .append("  </div>")
                .append("  <div class=\"cartilla-body\">")
                .append("    <p style=\"margin: 0 0 15px 0; font-size: 13px; color:#6b7280;\">")
                .append("      <strong>Iniciada:</strong> ").append(fechaInicio);
            
            if (!fechaFin.equalsIgnoreCase("N/A")) {
                html.append(" | <strong>Completada:</strong> ").append(fechaFin);
            }
            html.append("    </p>");

            // Mostrar progreso visual de Sellos si no está canjeada
            if (!estado.equalsIgnoreCase("canjeada")) {
                html.append("    <div class=\"progreso-seccion\">")
                    .append("      <div class=\"progreso-item\">")
                    .append("        <strong>Compras de Chifones (").append(chifonesNum).append("/10):</strong>")
                    .append("        <div class=\"stamp-grid\">");
                for (int s = 1; s <= 10; s++) {
                    if (s <= chifonesNum) {
                        html.append("<span class=\"stamp stamp-filled\"></span>");
                    } else {
                        html.append("<span class=\"stamp stamp-empty\">").append(s).append("</span>");
                    }
                }
                html.append("        </div>")
                    .append("      </div>");

                html.append("      <div class=\"progreso-item\" style=\"margin-top: 15px;\">")
                    .append("        <strong>Envases Devueltos (").append(envasesNum).append("/10):</strong>")
                    .append("        <div class=\"stamp-grid\">");
                for (int s = 1; s <= 10; s++) {
                    if (s <= envasesNum) {
                        html.append("<span class=\"stamp stamp-filled\">♻</span>");
                    } else {
                        html.append("<span class=\"stamp stamp-empty\">").append(s).append("</span>");
                    }
                }
                html.append("        </div>")
                    .append("      </div>")
                    .append("    </div>");
                
                if (estado.equalsIgnoreCase("completada")) {
                    html.append("<div class=\"claim-box\">")
                        .append("  <strong>¡PREMIO HABILITADO!</strong><br>")
                        .append("  Ya puedes reclamar tu chifón tradicional de regalo. Envía el comando:<br>")
                        .append("  <span class=\"code-pattern\">CANJEAR_PREMIO[\"ClienteID\", \"11\"]</span>")
                        .append("</div>");
                }
            } else {
                // Si ya fue canjeada, mostrar el premio detallado
                html.append("<div class=\"premio-box\">")
                    .append("  <h4>DETALLES DEL PREMIO ENTREGADO</h4>")
                    .append("  <table style=\"margin-top: 5px; border: none;\">")
                    .append("    <tr><td><strong>Sabor Obsequiado:</strong></td><td><span style=\"color:#7c3aed; font-weight:bold;\">").append(premioSabor).append("</span></td></tr>")
                    .append("    <tr><td><strong>Fecha de Reclamación:</strong></td><td>").append(fechaCanje).append("</td></tr>")
                    .append("    <tr><td><strong>Envase Devuelto:</strong></td><td>")
                    .append("      <span class=\"badge badge-").append(premioEnvase.equalsIgnoreCase("SÍ") ? "si" : "no").append("\">").append(premioEnvase).append("</span>")
                    .append("    </td></tr>")
                    .append("  </table>")
                    .append("</div>");
            }

            // Mostrar tablas de Pedidos y Envases de esta cartilla
            html.append("    <h4 class=\"seccion-card-title\">Pedidos en esta Cartilla</h4>")
                .append("    <table>")
                .append("      <tr><th>Pedido ID</th><th>Fecha</th><th>Total</th><th>Estado Pago</th><th>Detalles</th></tr>")
                .append(pedidosHtml.length() > 0 ? pedidosHtml.toString() : "<tr><td colspan=\"5\" style=\"text-align:center;color:#9ca3af;\">Ningún pedido en esta cartilla.</td></tr>")
                .append("    </table>");

            html.append("    <h4 class=\"seccion-card-title\" style=\"margin-top: 25px;\">Control de Envases</h4>")
                .append("    <table>")
                .append("      <tr><th>Tipo de Envase</th><th style=\"text-align:center;\">Prestados</th><th style=\"text-align:center;\">Devueltos</th><th>Fecha Devolución</th><th>Estado Envase</th></tr>")
                .append(envasesHtml.length() > 0 ? envasesHtml.toString() : "<tr><td colspan=\"5\" style=\"text-align:center;color:#9ca3af;\">Ningún envase prestado.</td></tr>")
                .append("    </table>")
                .append("  </div>")
                .append("</div>");
        }

        return html.toString();
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
               "      max-width: 750px;\n" +
               "      margin: 40px auto;\n" +
               "      background-color: #ffffff;\n" +
               "      border-radius: 20px;\n" +
               "      overflow: hidden;\n" +
               "      box-shadow: 0 10px 40px rgba(97, 56, 28, 0.08);\n" +
               "      border: 1px solid #f0e6df;\n" +
               "    }\n" +
               "    .header {\n" +
               "      background: linear-gradient(135deg, #a78bfa, #7c3aed);\n" +
               "      padding: 35px 20px;\n" +
               "      text-align: center;\n" +
               "      color: #ffffff;\n" +
               "    }\n" +
               "    .header h1 {\n" +
               "      margin: 0;\n" +
               "      font-size: 24px;\n" +
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
               "    .client-banner {\n" +
               "      background-color: #f3e8ff;\n" +
               "      border-left: 5px solid #7c3aed;\n" +
               "      padding: 15px 20px;\n" +
               "      border-radius: 8px;\n" +
               "      margin-bottom: 25px;\n" +
               "    }\n" +
               "    .client-banner h3 {\n" +
               "      margin: 0;\n" +
               "      color: #5b21b6;\n" +
               "      font-size: 17px;\n" +
               "    }\n" +
               "    .card-title {\n" +
               "      font-size: 19px;\n" +
               "      font-weight: 600;\n" +
               "      margin-top: 0;\n" +
               "      margin-bottom: 20px;\n" +
               "      color: #7c3aed;\n" +
               "      border-bottom: 2px solid #f7efe9;\n" +
               "      padding-bottom: 10px;\n" +
               "    }\n" +
               "    .seccion-card-title {\n" +
               "      font-size: 14px;\n" +
               "      font-weight: 600;\n" +
               "      margin: 20px 0 10px 0;\n" +
               "      color: #4b5563;\n" +
               "    }\n" +
               "    .cartilla-card {\n" +
               "      border: 1px solid #e5e7eb;\n" +
               "      border-radius: 12px;\n" +
               "      overflow: hidden;\n" +
               "      margin-bottom: 30px;\n" +
               "      box-shadow: 0 4px 6px rgba(0,0,0,0.02);\n" +
               "    }\n" +
               "    .cartilla-header {\n" +
               "      padding: 15px 20px;\n" +
               "      color: #ffffff;\n" +
               "      display: flex;\n" +
               "      justify-content: space-between;\n" +
               "      align-items: center;\n" +
               "    }\n" +
               "    .cartilla-activa {\n" +
               "      background: linear-gradient(135deg, #8b5cf6, #6d28d9);\n" +
               "    }\n" +
               "    .cartilla-completada {\n" +
               "      background: linear-gradient(135deg, #10b981, #047857);\n" +
               "    }\n" +
               "    .cartilla-canjeada {\n" +
               "      background: linear-gradient(135deg, #9ca3af, #4b5563);\n" +
               "    }\n" +
               "    .cartilla-title {\n" +
               "      font-weight: 750;\n" +
               "      font-size: 16px;\n" +
               "    }\n" +
               "    .cartilla-badge {\n" +
               "      font-size: 11px;\n" +
               "      background-color: rgba(255,255,255,0.2);\n" +
               "      padding: 4px 10px;\n" +
               "      border-radius: 20px;\n" +
               "      font-weight: 600;\n" +
               "    }\n" +
               "    .cartilla-body {\n" +
               "      padding: 20px;\n" +
               "      background-color: #ffffff;\n" +
               "    }\n" +
               "    .progreso-seccion {\n" +
               "      background-color: #f9fafb;\n" +
               "      border: 1px solid #f3f4f6;\n" +
               "      border-radius: 8px;\n" +
               "      padding: 15px;\n" +
               "      margin-bottom: 20px;\n" +
               "    }\n" +
               "    .progreso-item strong {\n" +
               "      display: block;\n" +
               "      font-size: 12px;\n" +
               "      color: #4b5563;\n" +
               "      margin-bottom: 8px;\n" +
               "    }\n" +
               "    .stamp-grid {\n" +
               "      display: flex;\n" +
               "      gap: 6px;\n" +
               "      flex-wrap: wrap;\n" +
               "    }\n" +
               "    .stamp {\n" +
               "      width: 34px;\n" +
               "      height: 34px;\n" +
               "      border-radius: 50%;\n" +
               "      display: flex;\n" +
               "      align-items: center;\n" +
               "      justify-content: center;\n" +
               "      font-size: 14px;\n" +
               "      font-weight: bold;\n" +
               "      border: 2px dashed #d1d5db;\n" +
               "      color: #9ca3af;\n" +
               "      background-color: #ffffff;\n" +
               "    }\n" +
               "    .stamp-filled {\n" +
               "      border: 2px solid #7c3aed;\n" +
               "      background-color: #ede9fe;\n" +
               "      transform: rotate(-10deg);\n" +
               "      box-shadow: 0 2px 4px rgba(124, 58, 237, 0.15);\n" +
               "    }\n" +
               "    .claim-box {\n" +
               "      background-color: #ecfdf5;\n" +
               "      border: 1px solid #a7f3d0;\n" +
               "      color: #065f46;\n" +
               "      padding: 15px;\n" +
               "      border-radius: 8px;\n" +
               "      margin: 15px 0;\n" +
               "      font-size: 13px;\n" +
               "      line-height: 1.5;\n" +
               "    }\n" +
               "    .premio-box {\n" +
               "      background-color: #faf5ff;\n" +
               "      border: 1px solid #e9d5ff;\n" +
               "      border-radius: 8px;\n" +
               "      padding: 15px;\n" +
               "      margin-bottom: 20px;\n" +
               "    }\n" +
               "    .premio-box h4 {\n" +
               "      margin: 0 0 10px 0;\n" +
               "      color: #6b21a8;\n" +
               "      font-size: 13px;\n" +
               "    }\n" +
               "    .premio-box table td {\n" +
               "      padding: 4px 8px;\n" +
               "      border: none;\n" +
               "      font-size: 13px;\n" +
               "    }\n" +
               "    .code-pattern {\n" +
               "      display: inline-block;\n" +
               "      font-family: monospace;\n" +
               "      background-color: #ffffff;\n" +
               "      padding: 3px 8px;\n" +
               "      border-radius: 4px;\n" +
               "      border: 1px solid #10b981;\n" +
               "      font-weight: bold;\n" +
               "      color: #047857;\n" +
               "      margin-top: 5px;\n" +
               "    }\n" +
               "    .alert {\n" +
               "      padding: 18px;\n" +
               "      border-radius: 14px;\n" +
               "      margin-bottom: 25px;\n" +
               "      font-size: 14px;\n" +
               "      line-height: 1.6;\n" +
               "    }\n" +
               "    .alert-success {\n" +
               "      background-color: #f5f3ff;\n" +
               "      border: 1px solid #ddd6fe;\n" +
               "      color: #5b21b6;\n" +
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
               "      margin-top: 10px;\n" +
               "      border-radius: 8px;\n" +
               "      overflow: hidden;\n" +
               "      border: 1px solid #e5e7eb;\n" +
               "    }\n" +
               "    th {\n" +
               "      background-color: #f3f4f6;\n" +
               "      color: #374151;\n" +
               "      font-weight: 600;\n" +
               "      text-align: left;\n" +
               "      padding: 10px 12px;\n" +
               "      font-size: 12px;\n" +
               "      border-bottom: 1px solid #e5e7eb;\n" +
               "    }\n" +
               "    td {\n" +
               "      padding: 10px 12px;\n" +
               "      border-bottom: 1px solid #f3f4f6;\n" +
               "      font-size: 12px;\n" +
               "      color: #4b5563;\n" +
               "    }\n" +
               "    tr:last-child td {\n" +
               "      border-bottom: none;\n" +
               "    }\n" +
               "    tr:nth-child(even) {\n" +
               "      background-color: #f9fafb;\n" +
               "    }\n" +
               "    .badge {\n" +
               "      display: inline-block;\n" +
               "      padding: 3px 8px;\n" +
               "      border-radius: 30px;\n" +
               "      font-size: 10px;\n" +
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
               "      <h1>CARTILLA DIGITAL DEL CLIENTE</h1>\n" +
               "      <p>Historial Consolidado de Pedidos y Envases</p>\n" +
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

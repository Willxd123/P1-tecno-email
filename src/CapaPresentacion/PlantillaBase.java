package CapaPresentacion;

public class PlantillaBase {

    public static String generarHtml(String tituloModulo, String comando, String resultado) {
        StringBuilder body = new StringBuilder();
        body.append("<h2 class=\"card-title\">").append(comando).append("</h2>");
        boolean esError = resultado.trim().toLowerCase().startsWith("error");
        if (resultado.contains("|")) {
            body.append(parsearTablaAscii(resultado));
        } else {
            String cls = esError ? "alert-error" : "alert-success";
            String titulo = esError ? "&#9888; OCURRI&#211; UN INCONVENIENTE" : "&#10003; OPERACI&#211;N EXITOSA";
            body.append("<div class=\"alert ").append(cls).append("\">")
                .append("<strong>").append(titulo).append("</strong><br>")
                .append(resultado.replace("\n", "<br>"))
                .append("</div>");
        }
        return construirPlantillaBase(tituloModulo, body.toString());
    }

    public static String parsearTablaAscii(String ascii) {
        String[] lineas = ascii.split("\n");
        StringBuilder html = new StringBuilder("<table>");
        boolean esCabecera = true;
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("---") || linea.startsWith("===") || linea.startsWith("Resultado")) continue;
            if (!linea.contains("|")) {
                html.append("</table><p style=\"font-style:italic;margin-top:8px;\">").append(linea).append("</p><table>");
                continue;
            }
            String[] celdas = linea.split("\\|");
            html.append("<tr>");
            for (String celda : celdas) {
                celda = celda.trim();
                if (esCabecera) {
                    html.append("<th>").append(celda).append("</th>");
                } else {
                    html.append("<td>").append(badge(celda)).append("</td>");
                }
            }
            html.append("</tr>");
            esCabecera = false;
        }
        html.append("</table>");
        return html.toString();
    }

    private static String badge(String val) {
        switch (val.toUpperCase()) {
            case "SÍ": case "SI": case "TRUE": return "<span class=\"badge badge-si\">SÍ</span>";
            case "NO": case "FALSE": return "<span class=\"badge badge-no\">NO</span>";
            case "PENDIENTE": return "<span class=\"badge badge-warn\">pendiente</span>";
            case "PAGADO": return "<span class=\"badge badge-si\">pagado</span>";
            case "ENTREGADO": return "<span class=\"badge badge-info\">entregado</span>";
            case "CANCELADO": return "<span class=\"badge badge-no\">cancelado</span>";
            case "CONTADO": return "<span class=\"badge badge-info\">contado</span>";
            case "CUOTAS": return "<span class=\"badge badge-warn\">cuotas</span>";
            case "CRÍTICO": case "CRITICO": return "<span class=\"badge badge-no\">CRÍTICO</span>";
            case "OK": return "<span class=\"badge badge-si\">OK</span>";
            case "PROPIETARIO": case "SECRETARIA": case "CLIENTE":
                return "<span class=\"badge badge-rol\">" + val + "</span>";
            default: return val;
        }
    }

    public static String construirPlantillaBase(String titulo, String contenido) {
        return "<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"utf-8\">\n<style>\n" +
            "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;background-color:#fdfaf7;color:#4a3e3d;margin:0;padding:0}\n" +
            ".container{max-width:680px;margin:40px auto;background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 10px 40px rgba(97,56,28,.08);border:1px solid #f0e6df}\n" +
            ".header{background:linear-gradient(135deg,#61381c,#8b5a2b);padding:35px 20px;text-align:center;color:#fff}\n" +
            ".header h1{margin:0;font-size:26px;font-weight:700;letter-spacing:.8px;text-shadow:0 2px 4px rgba(0,0,0,.15)}\n" +
            ".header p{margin:8px 0 0;font-size:14px;opacity:.9}\n" +
            ".content{padding:35px 30px}\n" +
            ".card-title{font-size:18px;font-weight:600;margin-top:0;margin-bottom:20px;color:#61381c;border-bottom:2px solid #f7efe9;padding-bottom:10px}\n" +
            ".alert{padding:18px;border-radius:14px;margin-bottom:25px;font-size:14px;line-height:1.6}\n" +
            ".alert-success{background:#f0fdf4;border:1px solid #bbf7d0;color:#166534}\n" +
            ".alert-error{background:#fef2f2;border:1px solid #fecaca;color:#991b1b}\n" +
            "table{width:100%;border-collapse:separate;border-spacing:0;margin-top:15px;border-radius:12px;overflow:hidden;border:1px solid #eedfd4}\n" +
            "th{background-color:#8b5a2b;color:#fff;font-weight:600;text-align:left;padding:13px 15px;font-size:13px;letter-spacing:.3px}\n" +
            "td{padding:12px 15px;border-bottom:1px solid #f7efe9;font-size:13px;color:#4a3e3d}\n" +
            "tr:last-child td{border-bottom:none}\n" +
            "tr:nth-child(even){background-color:#fdfbf9}\n" +
            ".badge{display:inline-block;padding:3px 9px;border-radius:30px;font-size:11px;font-weight:600;text-align:center}\n" +
            ".badge-si{background:#dcfce7;color:#15803d;border:1px solid #bbf7d0}\n" +
            ".badge-no{background:#f3f4f6;color:#4b5563;border:1px solid #e5e7eb}\n" +
            ".badge-warn{background:#fef3c7;color:#d97706;border:1px solid #fde68a}\n" +
            ".badge-info{background:#dbeafe;color:#1d4ed8;border:1px solid #bfdbfe}\n" +
            ".badge-rol{background:#ede9fe;color:#6d28d9;border:1px solid #ddd6fe}\n" +
            ".footer{background:#fcf8f5;padding:22px;text-align:center;font-size:12px;color:#8c7b70;border-top:1px solid #f0e6df}\n" +
            "</style>\n</head>\n<body>\n" +
            "<div class=\"container\">\n" +
            "<div class=\"header\"><h1>&#127856; REPOSTERÍA ZUZU &#127856;</h1><p>Sistema Automatizado por Correo Electrónico</p></div>\n" +
            "<div class=\"content\">" + contenido + "</div>\n" +
            "<div class=\"footer\"><strong>Grupo 16 - Tecnología Web (UAGRM)</strong><br>Este es un correo automático, por favor no lo respondas directamente.</div>\n" +
            "</div>\n</body>\n</html>";
    }
}

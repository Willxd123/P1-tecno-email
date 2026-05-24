package utils.analex;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador Léxico (Analex) para el procesamiento de comandos por correo.
 * Procesa el Asunto (Subject) del correo para extraer el comando y sus parámetros.
 * Formato esperado: COMANDO [param1] [param2] ...
 * Ejemplo: CU1-01 [Juan] [Perez] [71234567] [mypass] [Cliente]
 */
public class Analex {

    /**
     * Extrae el nombre del comando del asunto del correo.
     */
    public static String getComando(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return "";
        }
        int firstBracket = subject.indexOf('[');
        if (firstBracket != -1) {
            return subject.substring(0, firstBracket).trim();
        }
        return subject.trim();
    }

    /**
     * Extrae los parámetros encerrados en corchetes [ ].
     * Soporta formato multi-corchete: COMANDO [p1] [p2]
     * Y formato oficial: COMANDO["p1", "p2", ...] o COMANDO[p1, p2, ...]
     */
    public static List<String> getParametros(String subject) {
        List<String> parametros = new ArrayList<>();
        if (subject == null || subject.trim().isEmpty()) {
            return parametros;
        }
        
        subject = subject.trim();
        int firstBracket = subject.indexOf('[');
        int lastBracket = subject.lastIndexOf(']');
        if (firstBracket == -1 || lastBracket == -1 || lastBracket <= firstBracket) {
            return parametros;
        }

        String inner = subject.substring(firstBracket + 1, lastBracket).trim();

        // Si contiene "][" o "] [" significa que es el formato de múltiples corchetes
        if (inner.contains("][") || inner.contains("] [")) {
            boolean inBrackets = false;
            StringBuilder currentParam = new StringBuilder();
            for (int i = 0; i < subject.length(); i++) {
                char c = subject.charAt(i);
                if (c == '[' && !inBrackets) {
                    inBrackets = true;
                    currentParam.setLength(0); // Reiniciar el buffer
                } else if (c == ']' && inBrackets) {
                    inBrackets = false;
                    parametros.add(currentParam.toString().trim());
                } else if (inBrackets) {
                    currentParam.append(c);
                }
            }
        } else {
            // Formato oficial de un solo corchete con comas: ["p1", "p2", ...]
            boolean inQuotes = false;
            StringBuilder currentParam = new StringBuilder();
            for (int i = 0; i < inner.length(); i++) {
                char c = inner.charAt(i);
                if (c == '"') {
                    inQuotes = !inQuotes; // Ignorar comillas y alternar estado
                } else if (c == ',' && !inQuotes) {
                    // Separador de parámetro
                    parametros.add(currentParam.toString().trim());
                    currentParam.setLength(0);
                } else {
                    currentParam.append(c);
                }
            }
            // Añadir el último parámetro restante
            parametros.add(currentParam.toString().trim());
        }
        
        return parametros;
    }
}

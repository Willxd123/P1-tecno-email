package utils.analex;

import java.util.ArrayList;
import java.util.List;

/**
 * Analizador Léxico (Analex) para el procesamiento de comandos por correo.
 * Procesa el Asunto (Subject) del correo para extraer el comando y sus parámetros.
 * Formato esperado: COMANDO["p1", "p2", ...] o COMANDO
 */
public class Analex {

    /**
     * Valida la sintaxis del asunto del correo de manera estricta y detallada.
     * Lanza IllegalArgumentException si encuentra errores en el formato.
     */
    public static void validarSintaxis(String subject) throws IllegalArgumentException {
        if (subject == null || subject.isEmpty()) {
            throw new IllegalArgumentException("El asunto del correo no puede estar vacío.");
        }
        
        subject = subject.trim();
        
        int firstBracket = subject.indexOf('[');
        int lastBracket = subject.lastIndexOf(']');
        
        if (firstBracket == -1 && lastBracket == -1) {
            // Comando simple sin parámetros (ej: HELP, LISUSR, LISINS)
            if (!subject.matches("^[A-Za-z0-9_-]+$")) {
                throw new IllegalArgumentException("El comando simple contiene caracteres especiales, espacios o acentos no permitidos (ej: '" + subject + "'). Solo se permiten letras, números, guiones y guiones bajos.");
            }
            return;
        }
        
        // Si tiene al menos un corchete, validar
        if (firstBracket == -1 || lastBracket == -1) {
            throw new IllegalArgumentException("Falta un corchete de apertura '[' o de cierre ']' en el asunto del correo.");
        }
        
        // Verificar cantidad de corchetes
        int openBrackets = 0;
        int closeBrackets = 0;
        for (int i = 0; i < subject.length(); i++) {
            if (subject.charAt(i) == '[') openBrackets++;
            if (subject.charAt(i) == ']') closeBrackets++;
        }
        
        if (openBrackets != 1 || closeBrackets != 1) {
            throw new IllegalArgumentException("Sintaxis incorrecta: El comando debe contener exactamente un corchete de apertura '[' y uno de cierre ']'.");
        }
        
        if (lastBracket != subject.length() - 1) {
            throw new IllegalArgumentException("Caracteres adicionales no permitidos después del corchete de cierre ']'.");
        }
        
        // Validar el nombre del comando
        String cmd = subject.substring(0, firstBracket);
        if (cmd.isEmpty()) {
            throw new IllegalArgumentException("El nombre del comando no puede estar vacío antes del corchete '['.");
        }
        if (!cmd.matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("El nombre del comando '" + cmd + "' contiene caracteres inválidos, acentos (´) o espacios antes de '['. No se permiten espacios ni caracteres especiales en el comando.");
        }
        
        // Validar el contenido interno de los corchetes
        String inner = subject.substring(firstBracket + 1, lastBracket);
        
        boolean inQuotes = false;
        int paramCount = 0;
        boolean expectingComma = false; // true después de cerrar comillas de un parámetro, esperando ',' o ']'
        
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            int absolutePos = firstBracket + 1 + i + 1; // Posición humana 1-based en el asunto original
            
            if (inQuotes) {
                if (c == '"') {
                    inQuotes = false;
                    expectingComma = true;
                }
                // Dentro de comillas se permite cualquier caracter (incluso espacios, acentos, backticks como "Juan Ca´", etc.)
            } else {
                // Fuera de comillas
                if (c == '"') {
                    if (expectingComma) {
                        throw new IllegalArgumentException("Falta una coma ',' entre parámetros en la posición " + absolutePos + ".");
                    }
                    inQuotes = true;
                    paramCount++;
                } else if (c == ',') {
                    if (!expectingComma) {
                        throw new IllegalArgumentException("Coma ',' fuera de lugar o parámetro vacío en la posición " + absolutePos + ".");
                    }
                    expectingComma = false;
                } else if (Character.isWhitespace(c)) {
                    throw new IllegalArgumentException("Espacio no permitido fuera de las comillas en la posición " + absolutePos + ".");
                } else {
                    throw new IllegalArgumentException("Carácter no permitido '" + c + "' fuera de las comillas en la posición " + absolutePos + ".");
                }
            }
        }
        
        if (inQuotes) {
            throw new IllegalArgumentException("Comillas dobles no balanceadas. Asegúrate de cerrar todos los parámetros con comillas dobles (\").");
        }
        
        if (paramCount > 0 && !expectingComma) {
            throw new IllegalArgumentException("Sintaxis incorrecta. El último parámetro debe terminar con comillas dobles (\") antes del corchete ']'.");
        }
    }

    /**
     * Extrae el nombre del comando del asunto del correo.
     */
    public static String getComando(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return "";
        }
        subject = subject.trim();
        int firstBracket = subject.indexOf('[');
        if (firstBracket != -1) {
            return subject.substring(0, firstBracket).trim();
        }
        return subject;
    }

    /**
     * Extrae los parámetros encerrados en corchetes [ ].
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

        String inner = subject.substring(firstBracket + 1, lastBracket);
        if (inner.isEmpty()) {
            return parametros;
        }

        boolean inQuotes = false;
        StringBuilder currentParam = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '"') {
                if (inQuotes) {
                    // Cerrar comillas, añadir el parámetro acumulado
                    parametros.add(currentParam.toString());
                    currentParam.setLength(0);
                    inQuotes = false;
                } else {
                    // Abrir comillas
                    inQuotes = true;
                }
            } else if (inQuotes) {
                currentParam.append(c);
            }
        }
        return parametros;
    }
}

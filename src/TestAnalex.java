import utils.analex.Analex;
import java.util.List;

public class TestAnalex {
    public static void main(String[] args) {
        System.out.println("=== PRUEBAS UNITARIAS DE ANALEX ===");

        // Casos que DEBEN ser válidos
        probarCaso("HELP", true);
        probarCaso("LISUSR", true);
        probarCaso("CU1-01[\"Juan\",\"Perez\"]", true);
        probarCaso("CU1-01[\"Juan Carlos\",\"Perez\"]", true); // Espacio dentro de comillas es válido
        probarCaso("CU1-01[\"Juan\",\"Perez´\"]", true); // Backtick dentro de comillas es válido
        probarCaso("CU1-01[]", true); // Sin parámetros es válido

        // Casos que DEBEN fallar
        probarCaso("CU1-01 [\"Juan\"]", false); // Espacio antes del corchete
        probarCaso("CU1-01[\"Juan\" ,\"Perez\"]", false); // Espacio antes de la coma
        probarCaso("CU1-01[\"Juan\", \"Perez\"]", false); // Espacio después de la coma
        probarCaso("CU1-01[\"Juan\"´,\"Perez\"]", false); // Backtick por demás fuera de comillas
        probarCaso("CU1-01´[\"Juan\"]", false); // Backtick antes del corchete
        probarCaso("CU1-01[\"Juan\",]", false); // Coma final sin parámetro
        probarCaso("CU1-01[\"Juan\"", false); // Falta corchete de cierre
        probarCaso("CU1-01\"Juan\"]", false); // Falta corchete de apertura
        probarCaso("CU1-01[\"Juan]", false); // Comillas no cerradas
    }

    private static void probarCaso(String subject, boolean esperadoValido) {
        try {
            Analex.validarSintaxis(subject);
            if (esperadoValido) {
                String cmd = Analex.getComando(subject);
                List<String> params = Analex.getParametros(subject);
                System.out.println("VALIDO (Esperado): '" + subject + "' -> Comando: " + cmd + ", Params: " + params);
            } else {
                System.err.println("ERROR: Se aceptó como válido pero debió fallar: '" + subject + "'");
            }
        } catch (IllegalArgumentException e) {
            if (!esperadoValido) {
                System.out.println("RECHAZADO (Esperado): '" + subject + "' -> Error: " + e.getMessage());
            } else {
                System.err.println("ERROR: Fallo pero debió ser valido: '" + subject + "' -> Error: " + e.getMessage());
            }
        }
    }
}

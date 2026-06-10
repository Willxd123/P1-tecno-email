package CapaPresentacion.controladores;

import CapaNegocio.NRoles;
import CapaPresentacion.PRoles;
import java.util.List;

public class RolControlador {
    
    /**
     * Retorna verdadero si el comando pertenece al recurso de Roles.
     */
    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU9-01") || comando.equals("REGROL") || comando.equals("REGISTRAR_ROL") ||
               comando.equals("CU9-02") || comando.equals("EDTROL") || comando.equals("EDITAR_ROL") ||
               comando.equals("CU9-03") || comando.equals("DELROL") || comando.equals("ELIMINAR_ROL") ||
               comando.equals("CU9-04") || comando.equals("LISROL") || comando.equals("LISTAR_ROLES") ||
               comando.equals("CU9-05") || comando.equals("VERROL") || comando.equals("VER_ROL");
    }

    /**
     * Procesa la solicitud y enruta al mÃ©todo de negocio correspondiente, devolviendo HTML.
     */
    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;
        
        switch (comando.toUpperCase().trim()) {
            case "CU9-01":
            case "REGROL":
            case "REGISTRAR_ROL":
                rawResult = NRoles.registrarRol(parametros);
                break;

            case "CU9-02":
            case "EDTROL":
            case "EDITAR_ROL":
                rawResult = NRoles.editarRol(parametros);
                break;

            case "CU9-03":
            case "DELROL":
            case "ELIMINAR_ROL":
                rawResult = NRoles.eliminarRol(parametros);
                break;

            case "CU9-04":
            case "LISROL":
            case "LISTAR_ROLES":
                rawResult = NRoles.listarRoles(parametros);
                break;

            case "CU9-05":
            case "VERROL":
            case "VER_ROL":
                rawResult = NRoles.verRol(parametros);
                break;

            default:
                rawResult = "Error: Comando de rol no soportado.";
        }
        
        return PRoles.generarHtml(comando, rawResult);
    }
}


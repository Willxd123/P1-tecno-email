package CapaPresentacion.controllers;

import CapaNegocio.NRoles;
import CapaPresentacion.PRoles;
import java.util.List;

public class RolController {
    
    /**
     * Retorna verdadero si el comando pertenece al recurso de Roles.
     */
    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("REGROL") || comando.equals("REGISTRAR_ROL") ||
               comando.equals("EDTROL") || comando.equals("EDITAR_ROL") ||
               comando.equals("DELROL") || comando.equals("ELIMINAR_ROL") ||
               comando.equals("LISROL") || comando.equals("LISTAR_ROLES") ||
               comando.equals("VERROL") || comando.equals("VER_ROL");
    }

    /**
     * Procesa la solicitud y enruta al método de negocio correspondiente, devolviendo HTML.
     */
    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;
        
        switch (comando.toUpperCase().trim()) {
            case "REGROL":
            case "REGISTRAR_ROL":
                rawResult = NRoles.registrarRol(parametros);
                break;

            case "EDTROL":
            case "EDITAR_ROL":
                rawResult = NRoles.editarRol(parametros);
                break;

            case "DELROL":
            case "ELIMINAR_ROL":
                rawResult = NRoles.eliminarRol(parametros);
                break;

            case "LISROL":
            case "LISTAR_ROLES":
                rawResult = NRoles.listarRoles(parametros);
                break;

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

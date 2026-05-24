package CapaPresentacion.controllers;

import CapaNegocio.NUsuarios;
import CapaPresentacion.PUsuarios;
import java.util.List;

public class UsuarioController {
    
    /**
     * Retorna verdadero si el comando pertenece al recurso de Usuarios.
     */
    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU1-01") || comando.equals("REGISTRAR_USUARIO") || comando.equals("INSPER") ||
               comando.equals("CU1-02") || comando.equals("EDITAR_USUARIO") ||
               comando.equals("CU1-03") || comando.equals("CAMBIAR_PASSWORD") ||
               comando.equals("CU1-04") || comando.equals("DESACTIVAR_USUARIO") ||
               comando.equals("CU1-05") || comando.equals("LISTAR_USUARIOS") || comando.equals("LISPER") ||
               comando.equals("CU1-06") || comando.equals("BUSCAR_USUARIO") ||
               comando.equals("CU1-07") || comando.equals("VER_PERFIL");
    }

    /**
     * Procesa la solicitud y enruta al método de negocio correspondiente, devolviendo HTML.
     */
    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;
        
        switch (comando.toUpperCase().trim()) {
            case "CU1-01":
            case "REGISTRAR_USUARIO":
            case "INSPER":
                rawResult = NUsuarios.registrarUsuario(parametros);
                break;

            case "CU1-02":
            case "EDITAR_USUARIO":
                rawResult = NUsuarios.editarUsuario(parametros);
                break;

            case "CU1-03":
            case "CAMBIAR_PASSWORD":
                rawResult = NUsuarios.cambiarPassword(parametros);
                break;

            case "CU1-04":
            case "DESACTIVAR_USUARIO":
                rawResult = NUsuarios.desactivarUsuario(parametros);
                break;

            case "CU1-05":
            case "LISTAR_USUARIOS":
            case "LISPER":
                rawResult = NUsuarios.listarUsuarios(parametros);
                break;

            case "CU1-06":
            case "BUSCAR_USUARIO":
                rawResult = NUsuarios.buscarUsuario(parametros);
                break;

            case "CU1-07":
            case "VER_PERFIL":
                rawResult = NUsuarios.verPerfil(parametros);
                break;

            default:
                rawResult = "Error: Comando de usuario no soportado.";
        }
        
        return PUsuarios.generarHtml(comando, rawResult);
    }
}

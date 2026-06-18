package CapaPresentacion.controladores;

import CapaNegocio.NEnvases;
import CapaPresentacion.PEnvases;
import java.util.List;

public class EnvaseControlador {

    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU3-01") || comando.equals("REGISTRAR_ENVASE") ||
               comando.equals("CU3-02") || comando.equals("EDITAR_ENVASE") ||
               comando.equals("CU3-03") || comando.equals("LISTAR_ENVASES") || comando.equals("LISENV") ||
               comando.equals("CU3-04") || comando.equals("PRESTAMO_ENVASE") || comando.equals("REGISTRAR_PRESTAMO") ||
               comando.equals("CU3-05") || comando.equals("DEVOLUCION_ENVASE") || comando.equals("REGISTRAR_DEVOLUCION") ||
               comando.equals("CU3-06") || comando.equals("ENVASES_PENDIENTES") ||
               comando.equals("CU3-07") || comando.equals("HISTORIAL_ENVASES");
    }

    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;

        switch (comando.toUpperCase().trim()) {
            case "CU3-01":
            case "REGISTRAR_ENVASE":
                rawResult = NEnvases.registrarTipoEnvase(parametros);
                break;

            case "CU3-02":
            case "EDITAR_ENVASE":
                rawResult = NEnvases.editarTipoEnvase(parametros);
                break;

            case "CU3-03":
            case "LISTAR_ENVASES":
            case "LISENV":
                rawResult = NEnvases.listarEnvases();
                break;

            case "CU3-04":
            case "PRESTAMO_ENVASE":
            case "REGISTRAR_PRESTAMO":
                rawResult = NEnvases.registrarPrestamo(parametros);
                break;

            case "CU3-05":
            case "DEVOLUCION_ENVASE":
            case "REGISTRAR_DEVOLUCION":
                rawResult = NEnvases.registrarDevolucion(parametros);
                break;

            case "CU3-06":
            case "ENVASES_PENDIENTES":
                rawResult = NEnvases.verEnvasesPendientes();
                break;

            case "CU3-07":
            case "HISTORIAL_ENVASES":
                rawResult = NEnvases.verHistorialEnvasesCliente(parametros);
                break;

            default:
                rawResult = "Error: Comando de envase no soportado.";
        }

        return PEnvases.generarHtml(comando, rawResult);
    }
}


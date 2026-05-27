package CapaPresentacion.controllers;

import CapaNegocio.NInsumos;
import CapaPresentacion.PInsumos;
import java.util.List;

public class InsumoController {

    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU2-01") || comando.equals("REGISTRAR_INSUMO") ||
               comando.equals("CU2-02") || comando.equals("EDITAR_INSUMO") ||
               comando.equals("CU2-03") || comando.equals("LISTAR_INSUMOS") || comando.equals("LISINS") ||
               comando.equals("CU2-04") || comando.equals("ENTRADA_INSUMO") || comando.equals("REGISTRAR_ENTRADA") ||
               comando.equals("CU2-05") || comando.equals("AJUSTE_INSUMO") || comando.equals("REGISTRAR_AJUSTE") ||
               comando.equals("CU2-06") || comando.equals("MERMA_INSUMO") || comando.equals("REGISTRAR_MERMA") ||
               comando.equals("CU2-07") || comando.equals("HISTORIAL_INSUMO") ||
               comando.equals("CU2-08") || comando.equals("ALERTAS_INSUMO") || comando.equals("ALEINS");
    }

    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;

        switch (comando.toUpperCase().trim()) {
            case "CU2-01":
            case "REGISTRAR_INSUMO":
                rawResult = NInsumos.registrarInsumo(parametros);
                break;

            case "CU2-02":
            case "EDITAR_INSUMO":
                rawResult = NInsumos.editarInsumo(parametros);
                break;

            case "CU2-03":
            case "LISTAR_INSUMOS":
            case "LISINS":
                rawResult = NInsumos.listarInsumos();
                break;

            case "CU2-04":
            case "ENTRADA_INSUMO":
            case "REGISTRAR_ENTRADA":
                rawResult = NInsumos.registrarEntrada(parametros);
                break;

            case "CU2-05":
            case "AJUSTE_INSUMO":
            case "REGISTRAR_AJUSTE":
                rawResult = NInsumos.registrarAjuste(parametros);
                break;

            case "CU2-06":
            case "MERMA_INSUMO":
            case "REGISTRAR_MERMA":
                rawResult = NInsumos.registrarMerma(parametros);
                break;

            case "CU2-07":
            case "HISTORIAL_INSUMO":
                rawResult = NInsumos.verHistorialMovimientos(parametros);
                break;

            case "CU2-08":
            case "ALERTAS_INSUMO":
            case "ALEINS":
                rawResult = NInsumos.verAlertasReposicion();
                break;

            default:
                rawResult = "Error: Comando de insumo no soportado.";
        }

        return PInsumos.generarHtml(comando, rawResult);
    }
}

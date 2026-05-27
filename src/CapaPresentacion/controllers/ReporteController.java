package CapaPresentacion.controllers;

import CapaNegocio.NReportes;
import CapaPresentacion.PReportes;
import java.util.List;

public class ReporteController {

    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU8-01") || comando.equals("REPORTE_VENTAS") ||
               comando.equals("CU8-02") || comando.equals("REPORTE_INGRESOS") ||
               comando.equals("CU8-03") || comando.equals("REPORTE_CUOTAS_PENDIENTES") ||
               comando.equals("CU8-04") || comando.equals("REPORTE_CONSUMO_INSUMOS") ||
               comando.equals("CU8-06") || comando.equals("REPORTE_STOCK_CRITICO") ||
               comando.equals("CU8-07") || comando.equals("REPORTE_ENVASES_PRESTADOS");
    }

    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;

        switch (comando.toUpperCase().trim()) {
            case "CU8-01":
            case "REPORTE_VENTAS":
                rawResult = NReportes.reporteVentas(parametros);
                break;
            case "CU8-02":
            case "REPORTE_INGRESOS":
                rawResult = NReportes.reporteContadoVsCredito(parametros);
                break;
            case "CU8-03":
            case "REPORTE_CUOTAS_PENDIENTES":
                rawResult = NReportes.reporteCuotasPendientes();
                break;
            case "CU8-04":
            case "REPORTE_CONSUMO_INSUMOS":
                rawResult = NReportes.reporteConsumoInsumos(parametros);
                break;
            case "CU8-06":
            case "REPORTE_STOCK_CRITICO":
                rawResult = NReportes.reporteStockCritico();
                break;
            case "CU8-07":
            case "REPORTE_ENVASES_PRESTADOS":
                rawResult = NReportes.reporteEnvasesPrestados();
                break;
            default:
                rawResult = "Error: Comando de reporte no soportado.";
        }

        return PReportes.generarHtml(comando, rawResult);
    }
}

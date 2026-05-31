package CapaPresentacion.controllers;

import CapaNegocio.NReportes;
import CapaPresentacion.PReportes;
import java.util.List;

public class ReporteController {
    public static boolean canHandle(String cmd) {
        if (cmd == null) return false;
        cmd = cmd.toUpperCase().trim();
        return cmd.equals("CU8_01") || cmd.equals("REPVENTAS") ||
               cmd.equals("CU8_02") || cmd.equals("REPINGRES") ||
               cmd.equals("CU8_03") || cmd.equals("REPCUOPEND") ||
               cmd.equals("CU8_04") || cmd.equals("REPCONINS") ||
               cmd.equals("CU8_05") || cmd.equals("REPCOSTO") ||
               cmd.equals("CU8_06") || cmd.equals("REPSTOCK") ||
               cmd.equals("CU8_07") || cmd.equals("REPENVPRES") ||
               cmd.equals("CU8_08") || cmd.equals("REPCLIFRE") ||
               cmd.equals("CU8_09") || cmd.equals("REPPROVEND");
    }

    public static String handle(String cmd, List<String> p) {
        String r;
        switch (cmd.toUpperCase().trim()) {
            case "CU8_01": case "REPVENTAS":   r = NReportes.reporteVentas(p); break;
            case "CU8_02": case "REPINGRES":   r = NReportes.reporteIngresos(p); break;
            case "CU8_03": case "REPCUOPEND":  r = NReportes.reporteCuotasPendientes(p); break;
            case "CU8_04": case "REPCONINS":   r = NReportes.reporteConsumoInsumos(p); break;
            case "CU8_05": case "REPCOSTO":    r = NReportes.reporteCostoProduccion(p); break;
            case "CU8_06": case "REPSTOCK":    r = NReportes.reporteStockCritico(p); break;
            case "CU8_07": case "REPENVPRES":  r = NReportes.reporteEnvasesPrestados(p); break;
            case "CU8_08": case "REPCLIFRE":   r = NReportes.reporteClientesFrecuentes(p); break;
            case "CU8_09": case "REPPROVEND":  r = NReportes.reporteProductosVendidos(p); break;
            default: r = "Error: Comando de reportes no soportado.";
        }
        return PReportes.generarHtml(cmd, r);
    }
}

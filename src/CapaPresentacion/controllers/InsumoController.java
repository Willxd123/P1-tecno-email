package CapaPresentacion.controllers;

import CapaNegocio.NInsumos;
import CapaPresentacion.PInsumos;
import java.util.List;

public class InsumoController {
    public static boolean canHandle(String cmd) {
        if (cmd == null) return false;
        cmd = cmd.toUpperCase().trim();
        return cmd.equals("CU2_01") || cmd.equals("REGINSM") ||
               cmd.equals("CU2_02") || cmd.equals("EDTINSM") ||
               cmd.equals("CU2_03") || cmd.equals("LISINSM") ||
               cmd.equals("CU2_04") || cmd.equals("ENTINSM") ||
               cmd.equals("CU2_05") || cmd.equals("AJUINSM") ||
               cmd.equals("CU2_06") || cmd.equals("MERINSM") ||
               cmd.equals("CU2_07") || cmd.equals("HISINSM") ||
               cmd.equals("CU2_08") || cmd.equals("ALEREP") ||
               cmd.equals("CU2_09") || cmd.equals("REGREC") ||
               cmd.equals("CU2_10") || cmd.equals("EDTREC") ||
               cmd.equals("CU2_11") || cmd.equals("VERREC");
    }

    public static String handle(String cmd, List<String> p) {
        String r;
        switch (cmd.toUpperCase().trim()) {
            case "CU2_01": case "REGINSM":  r = NInsumos.registrarInsumo(p); break;
            case "CU2_02": case "EDTINSM":  r = NInsumos.editarInsumo(p); break;
            case "CU2_03": case "LISINSM":  r = NInsumos.listarInsumos(p); break;
            case "CU2_04": case "ENTINSM":  r = NInsumos.entradaStock(p); break;
            case "CU2_05": case "AJUINSM":  r = NInsumos.ajusteStock(p); break;
            case "CU2_06": case "MERINSM":  r = NInsumos.mermaStock(p); break;
            case "CU2_07": case "HISINSM":  r = NInsumos.historialMovimientos(p); break;
            case "CU2_08": case "ALEREP":   r = NInsumos.alertasReposicion(p); break;
            case "CU2_09": case "REGREC":   r = NInsumos.registrarReceta(p); break;
            case "CU2_10": case "EDTREC":   r = NInsumos.editarReceta(p); break;
            case "CU2_11": case "VERREC":   r = NInsumos.verReceta(p); break;
            default: r = "Error: Comando de insumos no soportado.";
        }
        return PInsumos.generarHtml(cmd, r);
    }
}

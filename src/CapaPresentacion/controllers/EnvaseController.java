package CapaPresentacion.controllers;

import CapaNegocio.NEnvases;
import CapaPresentacion.PEnvases;
import java.util.List;

public class EnvaseController {
    public static boolean canHandle(String cmd) {
        if (cmd == null) return false;
        cmd = cmd.toUpperCase().trim();
        return cmd.equals("CU3_01") || cmd.equals("REGENV") ||
               cmd.equals("CU3_02") || cmd.equals("EDTENV") ||
               cmd.equals("CU3_03") || cmd.equals("LISENV") ||
               cmd.equals("CU3_04") || cmd.equals("PRESENV") ||
               cmd.equals("CU3_05") || cmd.equals("DEVENV") ||
               cmd.equals("CU3_06") || cmd.equals("PENDENV") ||
               cmd.equals("CU3_07") || cmd.equals("HISENV");
    }

    public static String handle(String cmd, List<String> p) {
        String r;
        switch (cmd.toUpperCase().trim()) {
            case "CU3_01": case "REGENV":   r = NEnvases.registrarTipoEnvase(p); break;
            case "CU3_02": case "EDTENV":   r = NEnvases.editarTipoEnvase(p); break;
            case "CU3_03": case "LISENV":   r = NEnvases.verStockEnvases(p); break;
            case "CU3_04": case "PRESENV":  r = NEnvases.registrarPrestamo(p); break;
            case "CU3_05": case "DEVENV":   r = NEnvases.registrarDevolucion(p); break;
            case "CU3_06": case "PENDENV":  r = NEnvases.verEnvasesPendientes(p); break;
            case "CU3_07": case "HISENV":   r = NEnvases.verHistorialEnvasesCliente(p); break;
            default: r = "Error: Comando de envases no soportado.";
        }
        return PEnvases.generarHtml(cmd, r);
    }
}

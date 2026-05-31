package CapaPresentacion.controllers;

import CapaNegocio.NCartilla;
import CapaPresentacion.PCartilla;
import java.util.List;

public class CartillaController {
    public static boolean canHandle(String cmd) {
        if (cmd == null) return false;
        cmd = cmd.toUpperCase().trim();
        return cmd.equals("CU4_01") || cmd.equals("CARTILLA") ||
               cmd.equals("CU4_02") || cmd.equals("BUSCART") ||
               cmd.equals("CU4_03") || cmd.equals("DETCART") ||
               cmd.equals("CU4_04") || cmd.equals("CUOTACART") ||
               cmd.equals("CU4_05") || cmd.equals("ENVCART");
    }

    public static String handle(String cmd, List<String> p) {
        String r;
        switch (cmd.toUpperCase().trim()) {
            case "CU4_01": case "CARTILLA":   r = NCartilla.verCartilla(p); break;
            case "CU4_02": case "BUSCART":    r = NCartilla.buscarClienteCartilla(p); break;
            case "CU4_03": case "DETCART":    r = NCartilla.verDetallePedidoCartilla(p); break;
            case "CU4_04": case "CUOTACART":  r = NCartilla.verCuotasCartilla(p); break;
            case "CU4_05": case "ENVCART":    r = NCartilla.verEnvasesCartilla(p); break;
            default: r = "Error: Comando de cartilla no soportado.";
        }
        return PCartilla.generarHtml(cmd, r);
    }
}

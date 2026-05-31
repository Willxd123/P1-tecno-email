package CapaPresentacion.controllers;

import CapaNegocio.NProductos;
import CapaPresentacion.PProductos;
import java.util.List;

public class ProductoController {
    public static boolean canHandle(String cmd) {
        if (cmd == null) return false;
        cmd = cmd.toUpperCase().trim();
        return cmd.equals("CU5_01") || cmd.equals("REGPRO") ||
               cmd.equals("CU5_02") || cmd.equals("EDTPRO") ||
               cmd.equals("CU5_03") || cmd.equals("TOGPRO") ||
               cmd.equals("CU5_04") || cmd.equals("LISPRO") ||
               cmd.equals("CU5_05") || cmd.equals("COSTPRO") ||
               cmd.equals("CU5_06") || cmd.equals("DISPRO");
    }

    public static String handle(String cmd, List<String> p) {
        String r;
        switch (cmd.toUpperCase().trim()) {
            case "CU5_01": case "REGPRO":   r = NProductos.registrarProducto(p); break;
            case "CU5_02": case "EDTPRO":   r = NProductos.editarProducto(p); break;
            case "CU5_03": case "TOGPRO":   r = NProductos.toggleProducto(p); break;
            case "CU5_04": case "LISPRO":   r = NProductos.listarProductos(p); break;
            case "CU5_05": case "COSTPRO":  r = NProductos.verCostoProduccion(p); break;
            case "CU5_06": case "DISPRO":   r = NProductos.verificarDisponibilidad(p); break;
            default: r = "Error: Comando de productos no soportado.";
        }
        return PProductos.generarHtml(cmd, r);
    }
}

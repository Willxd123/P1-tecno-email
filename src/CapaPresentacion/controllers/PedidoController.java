package CapaPresentacion.controllers;

import CapaNegocio.NPedidos;
import CapaPresentacion.PPedidos;
import java.util.List;

public class PedidoController {
    public static boolean canHandle(String cmd) {
        if (cmd == null) return false;
        cmd = cmd.toUpperCase().trim();
        return cmd.equals("CU6_01") || cmd.equals("CRPEDIDO") ||
               cmd.equals("CU6_02") || cmd.equals("DETPEDIDO") ||
               cmd.equals("CU6_03") || cmd.equals("LISPEDIDO") ||
               cmd.equals("CU6_04") || cmd.equals("EDTPEDIDO") ||
               cmd.equals("CU6_05") || cmd.equals("CELPEDIDO") ||
               cmd.equals("CU6_06") || cmd.equals("ENTPEDIDO");
    }

    public static String handle(String cmd, List<String> p) {
        String r;
        switch (cmd.toUpperCase().trim()) {
            case "CU6_01": case "CRPEDIDO":   r = NPedidos.crearPedido(p); break;
            case "CU6_02": case "DETPEDIDO":  r = NPedidos.verDetallePedido(p); break;
            case "CU6_03": case "LISPEDIDO":  r = NPedidos.listarPedidos(p); break;
            case "CU6_04": case "EDTPEDIDO":  r = NPedidos.cambiarEstado(p); break;
            case "CU6_05": case "CELPEDIDO":  r = NPedidos.cancelarPedido(p); break;
            case "CU6_06": case "ENTPEDIDO":  r = NPedidos.confirmarEntrega(p); break;
            default: r = "Error: Comando de pedidos no soportado.";
        }
        return PPedidos.generarHtml(cmd, r);
    }
}

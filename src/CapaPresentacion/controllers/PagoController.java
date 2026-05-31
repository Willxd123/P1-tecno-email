package CapaPresentacion.controllers;

import CapaNegocio.NPagos;
import CapaPresentacion.PPagos;
import java.util.List;

public class PagoController {
    public static boolean canHandle(String cmd) {
        if (cmd == null) return false;
        cmd = cmd.toUpperCase().trim();
        return cmd.equals("CU7_01") || cmd.equals("PAGCONTADO") ||
               cmd.equals("CU7_02") || cmd.equals("PAGCUOTAS") ||
               cmd.equals("CU7_03") || cmd.equals("PAGCUOTA") ||
               cmd.equals("CU7_04") || cmd.equals("VISCUOTAS") ||
               cmd.equals("CU7_05") || cmd.equals("CUOVENC") ||
               cmd.equals("CU7_06") || cmd.equals("CUOPROX") ||
               cmd.equals("CU7_07") || cmd.equals("RESPAGCLI");
    }

    public static String handle(String cmd, List<String> p) {
        String r;
        switch (cmd.toUpperCase().trim()) {
            case "CU7_01": case "PAGCONTADO": r = NPagos.confirmarPagoContado(p); break;
            case "CU7_02": case "PAGCUOTAS":  r = NPagos.configurarCuotas(p); break;
            case "CU7_03": case "PAGCUOTA":   r = NPagos.pagarCuota(p); break;
            case "CU7_04": case "VISCUOTAS":  r = NPagos.verCuotasPedido(p); break;
            case "CU7_05": case "CUOVENC":    r = NPagos.verCuotasVencidas(p); break;
            case "CU7_06": case "CUOPROX":    r = NPagos.verCuotasProximas(p); break;
            case "CU7_07": case "RESPAGCLI":  r = NPagos.resumenPagosCliente(p); break;
            default: r = "Error: Comando de pagos no soportado.";
        }
        return PPagos.generarHtml(cmd, r);
    }
}

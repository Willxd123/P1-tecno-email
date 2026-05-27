package CapaPresentacion.controllers;

import CapaNegocio.NPedidos;
import CapaPresentacion.PPedidos;
import java.util.List;

public class PedidoController {

    public static boolean canHandle(String comando) {
        if (comando == null) return false;
        comando = comando.toUpperCase().trim();
        return comando.equals("CU6-01") || comando.equals("CREAR_PEDIDO") ||
               comando.equals("CU6-02") || comando.equals("DETALLE_PEDIDO") ||
               comando.equals("CU6-03") || comando.equals("LISTAR_PEDIDOS") || comando.equals("LISPED") ||
               comando.equals("CU6-04") || comando.equals("CAMBIAR_ESTADO_PEDIDO") ||
               comando.equals("CU6-05") || comando.equals("CANCELAR_PEDIDO") ||
               comando.equals("CU7-03") || comando.equals("PAGAR_CUOTA") || comando.equals("REGISTRAR_PAGO_CUOTA") ||
               comando.equals("CU7-04") || comando.equals("ESTADO_CUOTAS") ||
               comando.equals("CU7-05") || comando.equals("CUOTAS_VENCIDAS");
    }

    public static String handle(String comando, List<String> parametros) {
        if (comando == null) return "Error: Comando nulo.";
        String rawResult;

        switch (comando.toUpperCase().trim()) {
            case "CU6-01":
            case "CREAR_PEDIDO":
                rawResult = NPedidos.crearPedido(parametros);
                break;

            case "CU6-02":
            case "DETALLE_PEDIDO":
                rawResult = NPedidos.verDetallePedido(parametros);
                break;

            case "CU6-03":
            case "LISTAR_PEDIDOS":
            case "LISPED":
                rawResult = NPedidos.listarPedidos();
                break;

            case "CU6-04":
            case "CAMBIAR_ESTADO_PEDIDO":
                rawResult = NPedidos.cambiarEstadoPedido(parametros);
                break;

            case "CU6-05":
            case "CANCELAR_PEDIDO":
                rawResult = NPedidos.cancelarPedido(parametros);
                break;

            case "CU7-03":
            case "PAGAR_CUOTA":
            case "REGISTRAR_PAGO_CUOTA":
                rawResult = NPedidos.registrarPagoCuota(parametros);
                break;

            case "CU7-04":
            case "ESTADO_CUOTAS":
                rawResult = NPedidos.verEstadoCuotas(parametros);
                break;

            case "CU7-05":
            case "CUOTAS_VENCIDAS":
                rawResult = NPedidos.verCuotasVencidas();
                break;

            default:
                rawResult = "Error: Comando de pedido/pago no soportado.";
        }

        return PPedidos.generarHtml(comando, rawResult);
    }
}

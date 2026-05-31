package CapaNegocio;

import CapaDatos.DCuotas;
import CapaDatos.DDetallePedido;
import CapaDatos.DEnvases;
import CapaDatos.DPagos;
import CapaDatos.DPedidoEnvase;
import CapaDatos.DPedidos;
import CapaDatos.DProductos;
import CapaDatos.DUsuarios;
import CapaDatos.enums.TipoPago;

import java.util.List;

public class NCartilla {

    // CU4_01: CARTILLA["usuario_id"]
    public static String verCartilla(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del usuario.";
        try {
            int usuarioId = Integer.parseInt(p.get(0).trim());
            DUsuarios usuario = DUsuarios.obtenerPorId(usuarioId);
            if (usuario == null) return "Error: No existe un usuario con ID " + usuarioId + ".";
            List<DPedidos> pedidos = DPedidos.listarPorUsuario(usuarioId);
            if (pedidos.isEmpty()) {
                return "El cliente '" + usuario.getNombre() + " " + usuario.getApellido() + "' no tiene pedidos registrados.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("=== CARTILLA DEL CLIENTE ===\n");
            sb.append("Cliente: ").append(usuario.getNombre()).append(" ").append(usuario.getApellido()).append("\n");
            sb.append("Email: ").append(usuario.getEmail()).append("\n\n");
            sb.append("Pedido # | Fecha | Estado | Total (Bs) | Tipo Pago | Cuotas Pendientes | Envases Prestados\n");
            sb.append("---\n");
            for (DPedidos ped : pedidos) {
                DPagos pago = DPagos.obtenerPorPedido(ped.getId());
                String tipoPago = pago != null ? pago.getTipo_pago().name() : "-";
                int cuotasPendientes = 0;
                if (pago != null && pago.getTipo_pago() == TipoPago.cuotas) {
                    List<DCuotas> cuotas = DCuotas.listarPorPago(pago.getId());
                    for (DCuotas c : cuotas) { if (!c.isPagado()) cuotasPendientes++; }
                }
                List<DPedidoEnvase> envases = DPedidoEnvase.listarPorPedidoOrigen(ped.getId());
                int envasesActivos = 0;
                for (DPedidoEnvase pe : envases) {
                    envasesActivos += (pe.getCantidad_prestada() - pe.getCantidad_devuelta());
                }
                sb.append(ped.getId()).append(" | ")
                  .append(ped.getFecha() != null ? ped.getFecha().toString().substring(0, 10) : "-").append(" | ")
                  .append(ped.getEstado().name()).append(" | ")
                  .append(String.format("%.2f", ped.getTotal())).append(" | ")
                  .append(tipoPago).append(" | ")
                  .append(cuotasPendientes).append(" | ")
                  .append(envasesActivos).append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU4_02: BUSCART["texto"]
    public static String buscarClienteCartilla(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere un texto de búsqueda.";
        try {
            String texto = p.get(0).trim().toLowerCase();
            List<DUsuarios> todos = DUsuarios.listar();
            StringBuilder sb = new StringBuilder();
            sb.append("Resultados de búsqueda para '").append(texto).append("':\n\n");
            sb.append("ID | Nombre | Teléfono | Email\n");
            sb.append("---\n");
            boolean encontrado = false;
            for (DUsuarios u : todos) {
                if (u.getNombre().toLowerCase().contains(texto) ||
                    u.getApellido().toLowerCase().contains(texto) ||
                    u.getTelefono().contains(texto) ||
                    String.valueOf(u.getId()).equals(texto)) {
                    sb.append(u.getId()).append(" | ")
                      .append(u.getNombre()).append(" ").append(u.getApellido()).append(" | ")
                      .append(u.getTelefono()).append(" | ")
                      .append(u.getEmail()).append("\n");
                    encontrado = true;
                }
            }
            if (!encontrado) return "No se encontraron clientes que coincidan con '" + texto + "'.";
            sb.append("\nUse CARTILLA[\"ID\"] para ver la cartilla completa de un cliente.");
            return sb.toString();
        } catch (Exception e) {
            return "Error al buscar cliente: " + e.getMessage();
        }
    }

    // CU4_03: DETCART["pedido_id"]
    public static String verDetallePedidoCartilla(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            DUsuarios usuario = DUsuarios.obtenerPorId(pedido.getUsuario_id());
            String nombreCliente = usuario != null ? usuario.getNombre() + " " + usuario.getApellido() : "ID:" + pedido.getUsuario_id();
            List<DDetallePedido> detalles = DDetallePedido.listarPorPedido(pedidoId);
            StringBuilder sb = new StringBuilder();
            sb.append("Detalle del Pedido #").append(pedidoId).append(" - ").append(nombreCliente).append("\n\n");
            sb.append("Producto | Cantidad | Precio Unit. (Bs) | Subtotal (Bs)\n");
            sb.append("---\n");
            double total = 0;
            for (DDetallePedido d : detalles) {
                DProductos prod = DProductos.obtenerPorId(d.getProducto_id());
                String nombreProd = prod != null ? prod.getNombre() : "ID:" + d.getProducto_id();
                double subtotal = d.getCantidad() * d.getPrecio_unitario();
                total += subtotal;
                sb.append(nombreProd).append(" | ")
                  .append(d.getCantidad()).append(" | ")
                  .append(String.format("%.2f", d.getPrecio_unitario())).append(" | ")
                  .append(String.format("%.2f", subtotal)).append("\n");
            }
            sb.append("\nTOTAL: Bs ").append(String.format("%.2f", total));
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU4_04: CUOTACART["pedido_id"]
    public static String verCuotasCartilla(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            DPagos pago = DPagos.obtenerPorPedido(pedidoId);
            if (pago == null) return "El pedido #" + pedidoId + " no tiene registro de pago.";
            if (pago.getTipo_pago() != TipoPago.cuotas) {
                return "El pedido #" + pedidoId + " es de pago al contado, no tiene cuotas.";
            }
            List<DCuotas> cuotas = DCuotas.listarPorPago(pago.getId());
            if (cuotas.isEmpty()) return "El pedido #" + pedidoId + " no tiene cuotas configuradas.";
            StringBuilder sb = new StringBuilder();
            sb.append("Cuotas del Pedido #").append(pedidoId).append("\n\n");
            sb.append("Cuota # | Monto (Bs) | Vencimiento | Fecha Pago | Pagado\n");
            sb.append("---\n");
            for (DCuotas c : cuotas) {
                sb.append(c.getNumero_cuota()).append(" | ")
                  .append(String.format("%.2f", c.getMonto_cuota())).append(" | ")
                  .append(c.getFecha_vencimiento()).append(" | ")
                  .append(c.getFecha_pago() != null ? c.getFecha_pago().toString() : "-").append(" | ")
                  .append(c.isPagado() ? "SÍ" : "NO").append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU4_05: ENVCART["pedido_id"]
    public static String verEnvasesCartilla(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            List<DPedidoEnvase> lista = DPedidoEnvase.listarPorPedidoOrigen(pedidoId);
            if (lista.isEmpty()) return "El pedido #" + pedidoId + " no tiene envases registrados.";
            StringBuilder sb = new StringBuilder();
            sb.append("Envases del Pedido #").append(pedidoId).append("\n\n");
            sb.append("Envase | Prestados | Devueltos | Pendientes\n");
            sb.append("---\n");
            for (DPedidoEnvase pe : lista) {
                DEnvases env = DEnvases.obtenerPorId(pe.getEnvase_id());
                String nombreEnv = env != null ? env.getNombre() : "ID:" + pe.getEnvase_id();
                int pendiente = pe.getCantidad_prestada() - pe.getCantidad_devuelta();
                sb.append(nombreEnv).append(" | ")
                  .append(pe.getCantidad_prestada()).append(" | ")
                  .append(pe.getCantidad_devuelta()).append(" | ")
                  .append(pendiente).append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

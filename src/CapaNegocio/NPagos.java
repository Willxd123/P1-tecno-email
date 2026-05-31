package CapaNegocio;

import CapaDatos.DCuotas;
import CapaDatos.DPagos;
import CapaDatos.DPedidos;
import CapaDatos.DUsuarios;
import CapaDatos.enums.EstadoPedido;
import CapaDatos.enums.TipoPago;

import java.sql.Date;
import java.util.List;

public class NPagos {

    // CU7_01: PAGCONTADO["pedido_id"]
    public static String confirmarPagoContado(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            if (pedido.getEstado() != EstadoPedido.pendiente) {
                return "Error: El pedido no está en estado 'pendiente'. Estado actual: " + pedido.getEstado().name() + ".";
            }
            DPagos pago = DPagos.obtenerPorPedido(pedidoId);
            if (pago == null) return "Error: El pedido #" + pedidoId + " no tiene un registro de pago.";
            if (pago.getTipo_pago() != TipoPago.contado) {
                return "Error: Este pedido es de tipo '" + pago.getTipo_pago().name() + "', no 'contado'. Use PAGCUOTA para cuotas individuales.";
            }
            pedido.setEstado(EstadoPedido.pagado);
            if (pedido.modificar()) {
                return "Éxito: Pago al contado del pedido #" + pedidoId + " confirmado. Total: Bs " + String.format("%.2f", pedido.getTotal()) + ". Estado: pagado.";
            }
            return "Error: No se pudo confirmar el pago.";
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU7_02: PAGCUOTAS["pedido_id","num_cuotas","fechas_sep_puntoycoma"]
    public static String configurarCuotas(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: pedido_id, num_cuotas, fechas separadas por punto y coma.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            int numCuotas = Integer.parseInt(p.get(1).trim());
            String[] fechas = p.get(2).trim().split(";");
            if (numCuotas <= 0) return "Error: El número de cuotas debe ser mayor a 0.";
            if (fechas.length != numCuotas) return "Error: Se proporcionaron " + fechas.length + " fechas pero se indicaron " + numCuotas + " cuotas.";
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            DPagos pago = DPagos.obtenerPorPedido(pedidoId);
            if (pago == null) return "Error: El pedido #" + pedidoId + " no tiene registro de pago.";
            if (pago.getTipo_pago() != TipoPago.cuotas) return "Error: El pedido es de tipo 'contado'. No se pueden configurar cuotas.";
            // Eliminar cuotas existentes
            List<DCuotas> cuotasExistentes = DCuotas.listarPorPago(pago.getId());
            for (DCuotas c : cuotasExistentes) { c.eliminar(); }
            // Insertar nuevas cuotas
            double montoCuota = pedido.getTotal() / numCuotas;
            for (int i = 0; i < numCuotas; i++) {
                DCuotas cuota = new DCuotas();
                cuota.setNumero_cuota(i + 1);
                cuota.setMonto_cuota(montoCuota);
                cuota.setFecha_vencimiento(Date.valueOf(fechas[i].trim()));
                cuota.setFecha_pago(null);
                cuota.setPagado(false);
                cuota.setPago_id(pago.getId());
                cuota.insertar();
            }
            return "Éxito: " + numCuotas + " cuota(s) configuradas para el pedido #" + pedidoId + ". Monto por cuota: Bs " + String.format("%.2f", montoCuota) + ".";
        } catch (NumberFormatException e) {
            return "Error: IDs y número de cuotas deben ser numéricos.";
        } catch (IllegalArgumentException e) {
            return "Error: Formato de fecha inválido. Use YYYY-MM-DD.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU7_03: PAGCUOTA["cuota_id"]
    public static String pagarCuota(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID de la cuota.";
        try {
            int cuotaId = Integer.parseInt(p.get(0).trim());
            DCuotas cuota = DCuotas.obtenerPorId(cuotaId);
            if (cuota == null) return "Error: No existe una cuota con ID " + cuotaId + ".";
            if (cuota.isPagado()) return "Error: La cuota #" + cuota.getNumero_cuota() + " ya fue pagada.";
            cuota.setPagado(true);
            cuota.setFecha_pago(new Date(System.currentTimeMillis()));
            cuota.modificar();
            // Verificar si todas las cuotas del pago están pagadas
            List<DCuotas> todasCuotas = DCuotas.listarPorPago(cuota.getPago_id());
            boolean todasPagadas = true;
            for (DCuotas c : todasCuotas) { if (!c.isPagado()) { todasPagadas = false; break; } }
            String extra = "";
            if (todasPagadas) {
                DPagos pago = DPagos.obtenerPorId(cuota.getPago_id());
                if (pago != null) {
                    DPedidos pedido = DPedidos.obtenerPorId(pago.getPedido_id());
                    if (pedido != null) {
                        pedido.setEstado(EstadoPedido.pagado);
                        pedido.modificar();
                        extra = " Todas las cuotas del pedido #" + pedido.getId() + " están pagadas. Pedido marcado como PAGADO.";
                    }
                }
            }
            return "Éxito: Cuota #" + cuota.getNumero_cuota() + " (ID:" + cuotaId + ") pagada. Monto: Bs " + String.format("%.2f", cuota.getMonto_cuota()) + "." + extra;
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU7_04: VISCUOTAS["pedido_id"]
    public static String verCuotasPedido(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            DPagos pago = DPagos.obtenerPorPedido(pedidoId);
            if (pago == null) return "Error: El pedido #" + pedidoId + " no tiene registro de pago.";
            if (pago.getTipo_pago() != TipoPago.cuotas) return "El pedido #" + pedidoId + " es de pago al contado, no tiene cuotas.";
            List<DCuotas> cuotas = DCuotas.listarPorPago(pago.getId());
            if (cuotas.isEmpty()) return "El pedido #" + pedidoId + " no tiene cuotas configuradas. Use PAGCUOTAS para configurarlas.";
            StringBuilder sb = new StringBuilder();
            sb.append("Cuotas del Pedido #").append(pedidoId).append(" - Total: Bs ").append(String.format("%.2f", pedido.getTotal())).append("\n\n");
            sb.append("Cuota # | Monto (Bs) | Vencimiento | Fecha Pago | Estado\n");
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

    // CU7_05: CUOVENC
    public static String verCuotasVencidas(List<String> p) {
        try {
            List<DCuotas> cuotas = DCuotas.listarVencidas();
            if (cuotas.isEmpty()) return "No hay cuotas vencidas pendientes de pago.";
            StringBuilder sb = new StringBuilder();
            sb.append("Cuota ID | Pedido | Cliente | Monto (Bs) | Vencimiento\n");
            sb.append("---\n");
            for (DCuotas c : cuotas) {
                DPagos pago = DPagos.obtenerPorId(c.getPago_id());
                String pedidoInfo = "-";
                String clienteInfo = "-";
                if (pago != null) {
                    pedidoInfo = "#" + pago.getPedido_id();
                    DPedidos pedido = DPedidos.obtenerPorId(pago.getPedido_id());
                    if (pedido != null) {
                        DUsuarios usr = DUsuarios.obtenerPorId(pedido.getUsuario_id());
                        if (usr != null) clienteInfo = usr.getNombre() + " " + usr.getApellido();
                    }
                }
                sb.append(c.getId()).append(" | ")
                  .append(pedidoInfo).append(" | ")
                  .append(clienteInfo).append(" | ")
                  .append(String.format("%.2f", c.getMonto_cuota())).append(" | ")
                  .append(c.getFecha_vencimiento()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar cuotas vencidas: " + e.getMessage();
        }
    }

    // CU7_06: CUOPROX["dias"]
    public static String verCuotasProximas(List<String> p) {
        int dias = 7;
        if (!p.isEmpty()) {
            try { dias = Integer.parseInt(p.get(0).trim()); } catch (NumberFormatException ignored) {}
        }
        try {
            List<DCuotas> cuotas = DCuotas.listarProximasAVencer(dias);
            if (cuotas.isEmpty()) return "No hay cuotas próximas a vencer en los próximos " + dias + " días.";
            StringBuilder sb = new StringBuilder();
            sb.append("Cuotas próximas a vencer (").append(dias).append(" días):\n\n");
            sb.append("Cuota ID | Pedido | Cliente | Monto (Bs) | Vencimiento\n");
            sb.append("---\n");
            for (DCuotas c : cuotas) {
                DPagos pago = DPagos.obtenerPorId(c.getPago_id());
                String pedidoInfo = "-";
                String clienteInfo = "-";
                if (pago != null) {
                    pedidoInfo = "#" + pago.getPedido_id();
                    DPedidos pedido = DPedidos.obtenerPorId(pago.getPedido_id());
                    if (pedido != null) {
                        DUsuarios usr = DUsuarios.obtenerPorId(pedido.getUsuario_id());
                        if (usr != null) clienteInfo = usr.getNombre() + " " + usr.getApellido();
                    }
                }
                sb.append(c.getId()).append(" | ")
                  .append(pedidoInfo).append(" | ")
                  .append(clienteInfo).append(" | ")
                  .append(String.format("%.2f", c.getMonto_cuota())).append(" | ")
                  .append(c.getFecha_vencimiento()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar cuotas próximas: " + e.getMessage();
        }
    }

    // CU7_07: RESPAGCLI["usuario_id"]
    public static String resumenPagosCliente(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del usuario.";
        try {
            int usuarioId = Integer.parseInt(p.get(0).trim());
            DUsuarios usuario = DUsuarios.obtenerPorId(usuarioId);
            if (usuario == null) return "Error: No existe un usuario con ID " + usuarioId + ".";
            List<DPedidos> pedidos = DPedidos.listarPorUsuario(usuarioId);
            if (pedidos.isEmpty()) return "El cliente '" + usuario.getNombre() + " " + usuario.getApellido() + "' no tiene pedidos registrados.";
            double totalPagado = 0, totalPendiente = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("Resumen de Pagos - ").append(usuario.getNombre()).append(" ").append(usuario.getApellido()).append("\n\n");
            sb.append("Pedido # | Fecha | Total (Bs) | Tipo Pago | Estado\n");
            sb.append("---\n");
            for (DPedidos ped : pedidos) {
                DPagos pago = DPagos.obtenerPorPedido(ped.getId());
                String tipoPago = pago != null ? pago.getTipo_pago().name() : "-";
                sb.append(ped.getId()).append(" | ")
                  .append(ped.getFecha() != null ? ped.getFecha().toString().substring(0, 10) : "-").append(" | ")
                  .append(String.format("%.2f", ped.getTotal())).append(" | ")
                  .append(tipoPago).append(" | ")
                  .append(ped.getEstado().name()).append("\n");
                if (ped.getEstado() == EstadoPedido.pagado || ped.getEstado() == EstadoPedido.entregado) {
                    totalPagado += ped.getTotal();
                } else if (ped.getEstado() == EstadoPedido.pendiente) {
                    totalPendiente += ped.getTotal();
                }
            }
            sb.append("\nTotal Pagado: Bs ").append(String.format("%.2f", totalPagado));
            sb.append("\nTotal Pendiente: Bs ").append(String.format("%.2f", totalPendiente));
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

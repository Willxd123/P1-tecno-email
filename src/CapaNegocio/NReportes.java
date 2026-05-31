package CapaNegocio;

import CapaDatos.DCuotas;
import CapaDatos.DDetallePedido;
import CapaDatos.DEnvases;
import CapaDatos.DInsumos;
import CapaDatos.DMovimientosInsumo;
import CapaDatos.DPagos;
import CapaDatos.DPedidoEnvase;
import CapaDatos.DPedidos;
import CapaDatos.DProductos;
import CapaDatos.DUsuarios;
import CapaDatos.enums.EstadoPedido;
import CapaDatos.enums.TipoMovimientoInsumo;
import CapaDatos.enums.TipoPago;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NReportes {

    private static Timestamp[] parsearFechas(String inicio, String fin) {
        Timestamp tsInicio = Timestamp.valueOf(inicio.trim() + " 00:00:00");
        Timestamp tsFin = Timestamp.valueOf(fin.trim() + " 23:59:59");
        return new Timestamp[]{tsInicio, tsFin};
    }

    // CU8_01: REPVENTAS["fecha_inicio","fecha_fin"]
    public static String reporteVentas(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: fecha_inicio (YYYY-MM-DD) y fecha_fin (YYYY-MM-DD).";
        try {
            Timestamp[] ts = parsearFechas(p.get(0), p.get(1));
            List<DPedidos> pedidos = DPedidos.listarPorFechas(ts[0], ts[1]);
            if (pedidos.isEmpty()) return "No hay ventas registradas en el período " + p.get(0) + " a " + p.get(1) + ".";
            double totalVentas = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Ventas: ").append(p.get(0)).append(" al ").append(p.get(1)).append("\n\n");
            sb.append("Pedido # | Fecha | Cliente | Total (Bs) | Estado\n");
            sb.append("---\n");
            for (DPedidos ped : pedidos) {
                DUsuarios usr = DUsuarios.obtenerPorId(ped.getUsuario_id());
                String nombreUsr = usr != null ? usr.getNombre() + " " + usr.getApellido() : "ID:" + ped.getUsuario_id();
                sb.append(ped.getId()).append(" | ")
                  .append(ped.getFecha() != null ? ped.getFecha().toString().substring(0, 10) : "-").append(" | ")
                  .append(nombreUsr).append(" | ")
                  .append(String.format("%.2f", ped.getTotal())).append(" | ")
                  .append(ped.getEstado().name()).append("\n");
                if (ped.getEstado() != EstadoPedido.cancelado) totalVentas += ped.getTotal();
            }
            sb.append("\nTotal de pedidos: ").append(pedidos.size());
            sb.append("\nMonto total (excl. cancelados): Bs ").append(String.format("%.2f", totalVentas));
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "Error: Formato de fecha inválido. Use YYYY-MM-DD.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU8_02: REPINGRES["fecha_inicio","fecha_fin"]
    public static String reporteIngresos(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: fecha_inicio (YYYY-MM-DD) y fecha_fin (YYYY-MM-DD).";
        try {
            Timestamp[] ts = parsearFechas(p.get(0), p.get(1));
            List<DPedidos> pedidos = DPedidos.listarPorFechas(ts[0], ts[1]);
            double totalContado = 0, totalCuotas = 0;
            int cantContado = 0, cantCuotas = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Ingresos: ").append(p.get(0)).append(" al ").append(p.get(1)).append("\n\n");
            sb.append("Tipo Pago | Pedido # | Total (Bs) | Estado\n");
            sb.append("---\n");
            for (DPedidos ped : pedidos) {
                if (ped.getEstado() == EstadoPedido.cancelado) continue;
                DPagos pago = DPagos.obtenerPorPedido(ped.getId());
                String tipo = pago != null ? pago.getTipo_pago().name() : "-";
                sb.append(tipo).append(" | ")
                  .append(ped.getId()).append(" | ")
                  .append(String.format("%.2f", ped.getTotal())).append(" | ")
                  .append(ped.getEstado().name()).append("\n");
                if (pago != null && pago.getTipo_pago() == TipoPago.contado) {
                    totalContado += ped.getTotal(); cantContado++;
                } else if (pago != null && pago.getTipo_pago() == TipoPago.cuotas) {
                    totalCuotas += ped.getTotal(); cantCuotas++;
                }
            }
            sb.append("\nContado: ").append(cantContado).append(" pedido(s) - Bs ").append(String.format("%.2f", totalContado));
            sb.append("\nCuotas: ").append(cantCuotas).append(" pedido(s) - Bs ").append(String.format("%.2f", totalCuotas));
            sb.append("\nTOTAL INGRESOS: Bs ").append(String.format("%.2f", totalContado + totalCuotas));
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "Error: Formato de fecha inválido. Use YYYY-MM-DD.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU8_03: REPCUOPEND
    public static String reporteCuotasPendientes(List<String> p) {
        try {
            List<DCuotas> vencidas = DCuotas.listarVencidas();
            List<DCuotas> proximas = DCuotas.listarProximasAVencer(30);
            double totalAdeudado = 0;
            StringBuilder sb = new StringBuilder();
            sb.append("=== CUOTAS VENCIDAS ===\n");
            if (vencidas.isEmpty()) {
                sb.append("No hay cuotas vencidas.\n");
            } else {
                sb.append("Cuota ID | Pedido | Monto (Bs) | Vencimiento\n---\n");
                for (DCuotas c : vencidas) {
                    DPagos pago = DPagos.obtenerPorId(c.getPago_id());
                    String pedidoInfo = pago != null ? "#" + pago.getPedido_id() : "-";
                    sb.append(c.getId()).append(" | ").append(pedidoInfo).append(" | ")
                      .append(String.format("%.2f", c.getMonto_cuota())).append(" | ")
                      .append(c.getFecha_vencimiento()).append("\n");
                    totalAdeudado += c.getMonto_cuota();
                }
            }
            sb.append("\n=== CUOTAS PRÓXIMAS A VENCER (30 días) ===\n");
            if (proximas.isEmpty()) {
                sb.append("No hay cuotas próximas a vencer.\n");
            } else {
                sb.append("Cuota ID | Pedido | Monto (Bs) | Vencimiento\n---\n");
                for (DCuotas c : proximas) {
                    DPagos pago = DPagos.obtenerPorId(c.getPago_id());
                    String pedidoInfo = pago != null ? "#" + pago.getPedido_id() : "-";
                    sb.append(c.getId()).append(" | ").append(pedidoInfo).append(" | ")
                      .append(String.format("%.2f", c.getMonto_cuota())).append(" | ")
                      .append(c.getFecha_vencimiento()).append("\n");
                }
            }
            sb.append("\nMonto total adeudado (cuotas vencidas): Bs ").append(String.format("%.2f", totalAdeudado));
            return sb.toString();
        } catch (Exception e) {
            return "Error al generar reporte: " + e.getMessage();
        }
    }

    // CU8_04: REPCONINS["fecha_inicio","fecha_fin"]
    public static String reporteConsumoInsumos(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: fecha_inicio (YYYY-MM-DD) y fecha_fin (YYYY-MM-DD).";
        try {
            Timestamp tsInicio = Timestamp.valueOf(p.get(0).trim() + " 00:00:00");
            Timestamp tsFin = Timestamp.valueOf(p.get(1).trim() + " 23:59:59");
            List<DMovimientosInsumo> todos = DMovimientosInsumo.listarTodos();
            Map<Integer, Double> consumoPorInsumo = new HashMap<Integer, Double>();
            for (DMovimientosInsumo m : todos) {
                if (m.getTipo() == TipoMovimientoInsumo.consumo &&
                    m.getFecha() != null &&
                    !m.getFecha().before(tsInicio) &&
                    !m.getFecha().after(tsFin)) {
                    int insId = m.getInsumo_id();
                    double actual = consumoPorInsumo.containsKey(insId) ? consumoPorInsumo.get(insId) : 0.0;
                    consumoPorInsumo.put(insId, actual + Math.abs(m.getCantidad()));
                }
            }
            if (consumoPorInsumo.isEmpty()) return "No hubo consumo de insumos en el período " + p.get(0) + " a " + p.get(1) + ".";
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Consumo de Insumos: ").append(p.get(0)).append(" al ").append(p.get(1)).append("\n\n");
            sb.append("Insumo | Total Consumido | Unidad\n");
            sb.append("---\n");
            for (Map.Entry<Integer, Double> entry : consumoPorInsumo.entrySet()) {
                DInsumos ins = DInsumos.obtenerPorId(entry.getKey());
                String nombre = ins != null ? ins.getNombre() : "ID:" + entry.getKey();
                String unidad = ins != null ? ins.getUnidad_medida().name() : "-";
                sb.append(nombre).append(" | ")
                  .append(String.format("%.3f", entry.getValue())).append(" | ")
                  .append(unidad).append("\n");
            }
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "Error: Formato de fecha inválido. Use YYYY-MM-DD.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU8_05: REPCOSTO["fecha_inicio","fecha_fin"]
    public static String reporteCostoProduccion(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: fecha_inicio (YYYY-MM-DD) y fecha_fin (YYYY-MM-DD).";
        try {
            Timestamp tsInicio = Timestamp.valueOf(p.get(0).trim() + " 00:00:00");
            Timestamp tsFin = Timestamp.valueOf(p.get(1).trim() + " 23:59:59");
            List<DPedidos> pedidos = DPedidos.listarPorFechas(tsInicio, tsFin);
            double ingresoTotal = 0, costoTotal = 0;
            for (DPedidos ped : pedidos) {
                if (ped.getEstado() == EstadoPedido.cancelado) continue;
                ingresoTotal += ped.getTotal();
                List<DMovimientosInsumo> movs = DMovimientosInsumo.listarPorPedido(ped.getId());
                for (DMovimientosInsumo m : movs) {
                    if (m.getTipo() == TipoMovimientoInsumo.consumo) {
                        DInsumos ins = DInsumos.obtenerPorId(m.getInsumo_id());
                        if (ins != null) costoTotal += Math.abs(m.getCantidad()) * ins.getCosto_unitario();
                    }
                }
            }
            double margen = ingresoTotal - costoTotal;
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Costos de Producción: ").append(p.get(0)).append(" al ").append(p.get(1)).append("\n\n");
            sb.append("Concepto | Monto (Bs)\n---\n");
            sb.append("Ingresos por ventas | ").append(String.format("%.2f", ingresoTotal)).append("\n");
            sb.append("Costo de insumos | ").append(String.format("%.2f", costoTotal)).append("\n");
            sb.append("Margen bruto | ").append(String.format("%.2f", margen)).append("\n");
            sb.append("\nPedidos analizados: ").append(pedidos.size());
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "Error: Formato de fecha inválido. Use YYYY-MM-DD.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU8_06: REPSTOCK
    public static String reporteStockCritico(List<String> p) {
        try {
            List<DInsumos> lista = DInsumos.listarStockCritico();
            if (lista.isEmpty()) return "Todos los insumos tienen stock suficiente.";
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Stock Crítico\n\n");
            sb.append("ID | Nombre | Unidad | Stock Actual | Stock Mínimo | Faltante\n");
            sb.append("---\n");
            for (DInsumos ins : lista) {
                double faltante = ins.getStock_minimo() - ins.getStock_actual();
                sb.append(ins.getId()).append(" | ")
                  .append(ins.getNombre()).append(" | ")
                  .append(ins.getUnidad_medida().name()).append(" | ")
                  .append(String.format("%.3f", ins.getStock_actual())).append(" | ")
                  .append(String.format("%.3f", ins.getStock_minimo())).append(" | ")
                  .append(String.format("%.3f", faltante)).append("\n");
            }
            sb.append("\nTotal de insumos en estado crítico: ").append(lista.size());
            return sb.toString();
        } catch (Exception e) {
            return "Error al generar reporte: " + e.getMessage();
        }
    }

    // CU8_07: REPENVPRES
    public static String reporteEnvasesPrestados(List<String> p) {
        try {
            List<DPedidoEnvase> lista = DPedidoEnvase.listarPendientes();
            if (lista.isEmpty()) return "No hay envases pendientes de devolución.";
            long ahora = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Envases Prestados\n\n");
            sb.append("Envase | Cliente | Pedido | Prestados | Devueltos | Pendientes | Antigüedad (días)\n");
            sb.append("---\n");
            for (DPedidoEnvase pe : lista) {
                DEnvases env = DEnvases.obtenerPorId(pe.getEnvase_id());
                String nombreEnv = env != null ? env.getNombre() : "ID:" + pe.getEnvase_id();
                DPedidos pedido = DPedidos.obtenerPorId(pe.getPedido_origen_id());
                String nombreCliente = "-";
                long antiguedadDias = 0;
                if (pedido != null) {
                    DUsuarios usr = DUsuarios.obtenerPorId(pedido.getUsuario_id());
                    if (usr != null) nombreCliente = usr.getNombre() + " " + usr.getApellido();
                    if (pedido.getFecha() != null) {
                        antiguedadDias = (ahora - pedido.getFecha().getTime()) / (1000 * 60 * 60 * 24);
                    }
                }
                int pendiente = pe.getCantidad_prestada() - pe.getCantidad_devuelta();
                sb.append(nombreEnv).append(" | ")
                  .append(nombreCliente).append(" | ")
                  .append("#").append(pe.getPedido_origen_id()).append(" | ")
                  .append(pe.getCantidad_prestada()).append(" | ")
                  .append(pe.getCantidad_devuelta()).append(" | ")
                  .append(pendiente).append(" | ")
                  .append(antiguedadDias).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al generar reporte: " + e.getMessage();
        }
    }

    // CU8_08: REPCLIFRE["fecha_inicio","fecha_fin"]
    public static String reporteClientesFrecuentes(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: fecha_inicio (YYYY-MM-DD) y fecha_fin (YYYY-MM-DD).";
        try {
            Timestamp[] ts = parsearFechas(p.get(0), p.get(1));
            List<DPedidos> pedidos = DPedidos.listarPorFechas(ts[0], ts[1]);
            Map<Integer, Integer> conteo = new HashMap<Integer, Integer>();
            Map<Integer, Double> montos = new HashMap<Integer, Double>();
            for (DPedidos ped : pedidos) {
                if (ped.getEstado() == EstadoPedido.cancelado) continue;
                int uid = ped.getUsuario_id();
                conteo.put(uid, conteo.containsKey(uid) ? conteo.get(uid) + 1 : 1);
                montos.put(uid, (montos.containsKey(uid) ? montos.get(uid) : 0.0) + ped.getTotal());
            }
            if (conteo.isEmpty()) return "No hay datos de clientes en el período " + p.get(0) + " a " + p.get(1) + ".";
            // Ordenar por conteo desc (simple sort usando array)
            Integer[] clientes = conteo.keySet().toArray(new Integer[0]);
            for (int i = 0; i < clientes.length - 1; i++) {
                for (int j = i + 1; j < clientes.length; j++) {
                    if (conteo.get(clientes[j]) > conteo.get(clientes[i])) {
                        Integer temp = clientes[i]; clientes[i] = clientes[j]; clientes[j] = temp;
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Top Clientes Frecuentes: ").append(p.get(0)).append(" al ").append(p.get(1)).append("\n\n");
            sb.append("Cliente | Num. Pedidos | Monto Total (Bs)\n");
            sb.append("---\n");
            int top = Math.min(10, clientes.length);
            for (int i = 0; i < top; i++) {
                int uid = clientes[i];
                DUsuarios usr = DUsuarios.obtenerPorId(uid);
                String nombre = usr != null ? usr.getNombre() + " " + usr.getApellido() : "ID:" + uid;
                sb.append(nombre).append(" | ")
                  .append(conteo.get(uid)).append(" | ")
                  .append(String.format("%.2f", montos.get(uid))).append("\n");
            }
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "Error: Formato de fecha inválido. Use YYYY-MM-DD.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU8_09: REPPROVEND["fecha_inicio","fecha_fin"]
    public static String reporteProductosVendidos(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: fecha_inicio (YYYY-MM-DD) y fecha_fin (YYYY-MM-DD).";
        try {
            Timestamp[] ts = parsearFechas(p.get(0), p.get(1));
            List<DPedidos> pedidos = DPedidos.listarPorFechas(ts[0], ts[1]);
            Map<Integer, Integer> unidades = new HashMap<Integer, Integer>();
            Map<Integer, Double> montosProd = new HashMap<Integer, Double>();
            for (DPedidos ped : pedidos) {
                if (ped.getEstado() == EstadoPedido.cancelado) continue;
                List<DDetallePedido> detalles = DDetallePedido.listarPorPedido(ped.getId());
                for (DDetallePedido d : detalles) {
                    int pid = d.getProducto_id();
                    unidades.put(pid, (unidades.containsKey(pid) ? unidades.get(pid) : 0) + d.getCantidad());
                    montosProd.put(pid, (montosProd.containsKey(pid) ? montosProd.get(pid) : 0.0) + (d.getCantidad() * d.getPrecio_unitario()));
                }
            }
            if (unidades.isEmpty()) return "No hay productos vendidos en el período " + p.get(0) + " a " + p.get(1) + ".";
            // Ordenar por unidades desc
            Integer[] prods = unidades.keySet().toArray(new Integer[0]);
            for (int i = 0; i < prods.length - 1; i++) {
                for (int j = i + 1; j < prods.length; j++) {
                    if (unidades.get(prods[j]) > unidades.get(prods[i])) {
                        Integer temp = prods[i]; prods[i] = prods[j]; prods[j] = temp;
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Productos Vendidos: ").append(p.get(0)).append(" al ").append(p.get(1)).append("\n\n");
            sb.append("Producto | Unidades Vendidas | Monto Generado (Bs)\n");
            sb.append("---\n");
            for (int pid : prods) {
                DProductos prod = DProductos.obtenerPorId(pid);
                String nombre = prod != null ? prod.getNombre() : "ID:" + pid;
                sb.append(nombre).append(" | ")
                  .append(unidades.get(pid)).append(" | ")
                  .append(String.format("%.2f", montosProd.get(pid))).append("\n");
            }
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "Error: Formato de fecha inválido. Use YYYY-MM-DD.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

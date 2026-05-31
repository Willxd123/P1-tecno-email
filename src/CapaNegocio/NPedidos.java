package CapaNegocio;

import CapaDatos.DCuotas;
import CapaDatos.DDetallePedido;
import CapaDatos.DInsumos;
import CapaDatos.DMovimientosInsumo;
import CapaDatos.DPagos;
import CapaDatos.DPedidos;
import CapaDatos.DProductos;
import CapaDatos.DRecetaDetalle;
import CapaDatos.DRecetas;
import CapaDatos.DUsuarios;
import CapaDatos.enums.EstadoPedido;
import CapaDatos.enums.TipoMovimientoInsumo;
import CapaDatos.enums.TipoPago;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class NPedidos {

    /**
     * CU6_01: CRPEDIDO
     * Contado: ["usuario_id","contado","prod_id:cant",...]
     * Cuotas:  ["usuario_id","cuotas","num_cuotas","fecha1;fecha2;...","prod_id:cant",...]
     */
    public static String crearPedido(List<String> p) {
        if (p.size() < 3) return "Error: Parámetros insuficientes. Use CRPEDIDO[\"usuario_id\",\"tipo_pago\",\"prod_id:cant\",...].";
        try {
            int usuarioId = Integer.parseInt(p.get(0).trim());
            String tipoPagoStr = p.get(1).trim().toLowerCase();
            DUsuarios usuario = DUsuarios.obtenerPorId(usuarioId);
            if (usuario == null) return "Error: No existe un usuario con ID " + usuarioId + ".";

            TipoPago tipoPago;
            int numCuotas = 0;
            String[] fechasCuotas = null;
            int inicioProductos;

            if (tipoPagoStr.equals("cuotas")) {
                tipoPago = TipoPago.cuotas;
                if (p.size() < 5) return "Error: Para pago en cuotas se requiere: usuario_id, cuotas, num_cuotas, fechas_separadas_por_punto_y_coma, prod_id:cant.";
                numCuotas = Integer.parseInt(p.get(2).trim());
                if (numCuotas <= 0) return "Error: El número de cuotas debe ser mayor a 0.";
                fechasCuotas = p.get(3).trim().split(";");
                if (fechasCuotas.length != numCuotas) return "Error: Se proporcionaron " + fechasCuotas.length + " fechas pero se indicaron " + numCuotas + " cuotas.";
                inicioProductos = 4;
            } else if (tipoPagoStr.equals("contado")) {
                tipoPago = TipoPago.contado;
                inicioProductos = 2;
            } else {
                return "Error: Tipo de pago inválido. Use 'contado' o 'cuotas'.";
            }

            if (inicioProductos >= p.size()) return "Error: Se requiere al menos un producto en el pedido.";

            // Parsear productos
            List<int[]> productos = new ArrayList<>(); // [prod_id, cantidad]
            for (int i = inicioProductos; i < p.size(); i++) {
                String par = p.get(i).trim();
                if (!par.contains(":")) return "Error: Formato inválido para producto. Use 'prod_id:cantidad'.";
                String[] partes = par.split(":");
                int prodId = Integer.parseInt(partes[0].trim());
                int cant = Integer.parseInt(partes[1].trim());
                if (cant <= 0) return "Error: La cantidad del producto ID " + prodId + " debe ser mayor a 0.";
                DProductos prod = DProductos.obtenerPorId(prodId);
                if (prod == null) return "Error: No existe un producto con ID " + prodId + ".";
                if (!prod.isDisponible()) return "Error: El producto '" + prod.getNombre() + "' no está disponible.";
                productos.add(new int[]{prodId, cant});
            }

            // Calcular total
            double total = 0;
            for (int[] item : productos) {
                DProductos prod = DProductos.obtenerPorId(item[0]);
                total += prod.getPrecio_unitario() * item[1];
            }

            // Crear pedido
            DPedidos pedido = new DPedidos();
            pedido.setEstado(EstadoPedido.pendiente);
            pedido.setTotal(total);
            pedido.setUsuario_id(usuarioId);
            if (!pedido.insertar()) return "Error: No se pudo crear el pedido.";

            // Insertar detalles y descontar insumos
            for (int[] item : productos) {
                int prodId = item[0];
                int cant = item[1];
                DProductos prod = DProductos.obtenerPorId(prodId);

                DDetallePedido detalle = new DDetallePedido();
                detalle.setPedido_id(pedido.getId());
                detalle.setProducto_id(prodId);
                detalle.setCantidad(cant);
                detalle.setPrecio_unitario(prod.getPrecio_unitario());
                detalle.insertar();

                // Descontar insumos según receta
                DRecetas receta = DRecetas.obtenerPorProducto(prodId);
                if (receta != null) {
                    List<DRecetaDetalle> detallesReceta = DRecetaDetalle.listarPorReceta(receta.getId());
                    for (DRecetaDetalle rd : detallesReceta) {
                        double consumo = rd.getCantidad() * cant;
                        DInsumos ins = DInsumos.obtenerPorId(rd.getInsumo_id());
                        if (ins != null) {
                            DMovimientosInsumo mov = new DMovimientosInsumo();
                            mov.setTipo(TipoMovimientoInsumo.consumo);
                            mov.setCantidad(-consumo);
                            mov.setDescripcion("Consumo por pedido #" + pedido.getId() + " - " + prod.getNombre());
                            mov.setInsumo_id(ins.getId());
                            mov.setPedido_id(pedido.getId());
                            mov.insertar();
                            ins.setStock_actual(ins.getStock_actual() - consumo);
                            ins.modificar();
                        }
                    }
                }
            }

            // Crear pago
            DPagos pago = new DPagos();
            pago.setTipo_pago(tipoPago);
            pago.setPedido_id(pedido.getId());
            pago.insertar();

            // Crear cuotas si aplica
            if (tipoPago == TipoPago.cuotas) {
                double montoCuota = total / numCuotas;
                for (int i = 0; i < numCuotas; i++) {
                    DCuotas cuota = new DCuotas();
                    cuota.setNumero_cuota(i + 1);
                    cuota.setMonto_cuota(montoCuota);
                    cuota.setFecha_vencimiento(Date.valueOf(fechasCuotas[i].trim()));
                    cuota.setFecha_pago(null);
                    cuota.setPagado(false);
                    cuota.setPago_id(pago.getId());
                    cuota.insertar();
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Éxito: Pedido #").append(pedido.getId()).append(" creado correctamente.\n");
            sb.append("Cliente: ").append(usuario.getNombre()).append(" ").append(usuario.getApellido()).append("\n");
            sb.append("Total: Bs ").append(String.format("%.2f", total)).append("\n");
            sb.append("Tipo de pago: ").append(tipoPago.name());
            if (tipoPago == TipoPago.cuotas) {
                sb.append(" (").append(numCuotas).append(" cuotas de Bs ").append(String.format("%.2f", total / numCuotas)).append(")");
            }
            sb.append("\nEstado: pendiente");
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: Los IDs y cantidades deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU6_02: DETPEDIDO["pedido_id"]
    public static String verDetallePedido(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            DUsuarios usuario = DUsuarios.obtenerPorId(pedido.getUsuario_id());
            String nombreUsuario = usuario != null ? usuario.getNombre() + " " + usuario.getApellido() : "ID:" + pedido.getUsuario_id();
            List<DDetallePedido> detalles = DDetallePedido.listarPorPedido(pedidoId);
            DPagos pago = DPagos.obtenerPorPedido(pedidoId);

            StringBuilder sb = new StringBuilder();
            sb.append("=== PEDIDO #").append(pedidoId).append(" ===\n");
            sb.append("Cliente: ").append(nombreUsuario).append("\n");
            sb.append("Fecha: ").append(pedido.getFecha() != null ? pedido.getFecha().toString().substring(0, 16) : "-").append("\n");
            sb.append("Estado: ").append(pedido.getEstado().name()).append("\n");
            sb.append("Total: Bs ").append(String.format("%.2f", pedido.getTotal())).append("\n\n");

            sb.append("Producto | Cantidad | Precio Unit. | Subtotal\n");
            sb.append("---\n");
            for (DDetallePedido d : detalles) {
                DProductos prod = DProductos.obtenerPorId(d.getProducto_id());
                String nombreProd = prod != null ? prod.getNombre() : "ID:" + d.getProducto_id();
                double subtotal = d.getCantidad() * d.getPrecio_unitario();
                sb.append(nombreProd).append(" | ")
                  .append(d.getCantidad()).append(" | ")
                  .append(String.format("%.2f", d.getPrecio_unitario())).append(" | ")
                  .append(String.format("%.2f", subtotal)).append("\n");
            }

            if (pago != null) {
                sb.append("\nTipo de pago: ").append(pago.getTipo_pago().name());
                if (pago.getTipo_pago() == TipoPago.cuotas) {
                    List<DCuotas> cuotas = DCuotas.listarPorPago(pago.getId());
                    sb.append("\n\nCuota # | Monto | Vencimiento | Pagado\n---\n");
                    for (DCuotas c : cuotas) {
                        sb.append(c.getNumero_cuota()).append(" | ")
                          .append(String.format("%.2f", c.getMonto_cuota())).append(" | ")
                          .append(c.getFecha_vencimiento()).append(" | ")
                          .append(c.isPagado() ? "SÍ" : "NO").append("\n");
                    }
                }
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU6_03: LISPEDIDO[filtro] — filtro puede ser vacío, usuario_id, o estado
    public static String listarPedidos(List<String> p) {
        try {
            List<DPedidos> lista;
            String filtroDesc = "todos";
            if (!p.isEmpty()) {
                String filtro = p.get(0).trim();
                try {
                    int usuarioId = Integer.parseInt(filtro);
                    lista = DPedidos.listarPorUsuario(usuarioId);
                    filtroDesc = "usuario ID " + usuarioId;
                } catch (NumberFormatException ex) {
                    String[] estadosValidos = {"pendiente", "pagado", "cancelado", "entregado"};
                    boolean esEstado = false;
                    for (String e : estadosValidos) { if (e.equalsIgnoreCase(filtro)) { esEstado = true; break; } }
                    if (esEstado) {
                        lista = DPedidos.listarPorEstado(filtro.toLowerCase());
                        filtroDesc = "estado=" + filtro;
                    } else {
                        lista = DPedidos.listar();
                    }
                }
            } else {
                lista = DPedidos.listar();
            }

            if (lista.isEmpty()) return "No hay pedidos para el filtro: " + filtroDesc + ".";
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Fecha | Estado | Total (Bs) | Cliente | Tipo Pago\n");
            sb.append("---\n");
            for (DPedidos ped : lista) {
                DUsuarios usr = DUsuarios.obtenerPorId(ped.getUsuario_id());
                String nombreUsr = usr != null ? usr.getNombre() + " " + usr.getApellido() : "ID:" + ped.getUsuario_id();
                DPagos pago = DPagos.obtenerPorPedido(ped.getId());
                String tipoPago = pago != null ? pago.getTipo_pago().name() : "-";
                sb.append(ped.getId()).append(" | ")
                  .append(ped.getFecha() != null ? ped.getFecha().toString().substring(0, 10) : "-").append(" | ")
                  .append(ped.getEstado().name()).append(" | ")
                  .append(String.format("%.2f", ped.getTotal())).append(" | ")
                  .append(nombreUsr).append(" | ")
                  .append(tipoPago).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar pedidos: " + e.getMessage();
        }
    }

    // CU6_04: EDTPEDIDO["pedido_id","nuevo_estado"]
    public static String cambiarEstado(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: pedido_id, nuevo_estado.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            String nuevoEstadoStr = p.get(1).trim().toLowerCase();
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            EstadoPedido nuevoEstado;
            try {
                nuevoEstado = EstadoPedido.valueOf(nuevoEstadoStr);
            } catch (IllegalArgumentException e) {
                return "Error: Estado inválido. Valores válidos: pendiente, pagado, cancelado, entregado.";
            }
            EstadoPedido estadoAnterior = pedido.getEstado();
            pedido.setEstado(nuevoEstado);
            if (pedido.modificar()) {
                return "Éxito: Pedido #" + pedidoId + " cambiado de '" + estadoAnterior.name() + "' a '" + nuevoEstado.name() + "'.";
            }
            return "Error: No se pudo actualizar el estado del pedido.";
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU6_05: CELPEDIDO["pedido_id"]
    public static String cancelarPedido(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            if (pedido.getEstado() != EstadoPedido.pendiente) {
                return "Error: Solo se pueden cancelar pedidos en estado 'pendiente'. Estado actual: " + pedido.getEstado().name() + ".";
            }

            // Reponer insumos
            List<DDetallePedido> detalles = DDetallePedido.listarPorPedido(pedidoId);
            for (DDetallePedido d : detalles) {
                DRecetas receta = DRecetas.obtenerPorProducto(d.getProducto_id());
                if (receta != null) {
                    List<DRecetaDetalle> detallesReceta = DRecetaDetalle.listarPorReceta(receta.getId());
                    for (DRecetaDetalle rd : detallesReceta) {
                        double restituir = rd.getCantidad() * d.getCantidad();
                        DInsumos ins = DInsumos.obtenerPorId(rd.getInsumo_id());
                        if (ins != null) {
                            DMovimientosInsumo mov = new DMovimientosInsumo();
                            mov.setTipo(TipoMovimientoInsumo.ajuste);
                            mov.setCantidad(restituir);
                            mov.setDescripcion("Reposición por cancelación pedido #" + pedidoId);
                            mov.setInsumo_id(ins.getId());
                            mov.setPedido_id(pedidoId);
                            mov.insertar();
                            ins.setStock_actual(ins.getStock_actual() + restituir);
                            ins.modificar();
                        }
                    }
                }
            }

            pedido.setEstado(EstadoPedido.cancelado);
            if (pedido.modificar()) {
                return "Éxito: Pedido #" + pedidoId + " cancelado. Los insumos han sido repuestos al inventario.";
            }
            return "Error: No se pudo cancelar el pedido.";
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU6_06: ENTPEDIDO["pedido_id"]
    public static String confirmarEntrega(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del pedido.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            pedido.setEstado(EstadoPedido.entregado);
            if (pedido.modificar()) {
                return "Éxito: Pedido #" + pedidoId + " marcado como ENTREGADO.";
            }
            return "Error: No se pudo confirmar la entrega del pedido.";
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

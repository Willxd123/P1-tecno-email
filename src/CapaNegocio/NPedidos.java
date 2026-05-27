package CapaNegocio;

import CapaDatos.Conexion;
import CapaDatos.DPedidos;
import CapaDatos.DDetallePedido;
import CapaDatos.DPagos;
import CapaDatos.DCuotas;
import CapaDatos.DProductos;
import CapaDatos.DRecetas;
import CapaDatos.DRecetaDetalle;
import CapaDatos.DInsumos;
import CapaDatos.DMovimientosInsumo;
import CapaDatos.DUsuarios;
import CapaDatos.enums.EstadoPedido;
import CapaDatos.enums.TipoPago;
import CapaDatos.enums.TipoMovimientoInsumo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class NPedidos {

    // CU6-01: Crear Pedido
    // Params: ["ClienteID", "ProductoID", "Cantidad", "TipoPago", "CantCuotas"]
    // TipoPago: contado / cuotas
    // CantCuotas: 0 si es contado, >= 2 si es cuotas
    public static String crearPedido(List<String> parametros) {
        if (parametros.size() < 5) {
            return "Error: Faltan parámetros. Uso: CU6-01[\"ClienteID\",\"ProductoID\",\"Cantidad\",\"TipoPago\",\"CantCuotas\"]";
        }
        Connection conn = null;
        try {
            int clienteId = Integer.parseInt(parametros.get(0).trim());
            int productoId = Integer.parseInt(parametros.get(1).trim());
            int cantidad = Integer.parseInt(parametros.get(2).trim());
            String tipoPagoStr = parametros.get(3).trim().toLowerCase();
            int cantCuotas = Integer.parseInt(parametros.get(4).trim());

            // 1. Validaciones básicas
            DUsuarios cliente = DUsuarios.obtenerPorId(clienteId);
            if (cliente == null) {
                return "Error: El cliente con ID " + clienteId + " no está registrado.";
            }

            DProductos producto = DProductos.obtenerPorId(productoId);
            if (producto == null) {
                return "Error: El producto con ID " + productoId + " no existe.";
            }
            if (!producto.isDisponible()) {
                return "Error: El producto '" + producto.getNombre() + "' no está disponible actualmente.";
            }
            if (cantidad <= 0) {
                return "Error: La cantidad debe ser mayor a 0.";
            }

            TipoPago tipoPago;
            try {
                tipoPago = TipoPago.valueOf(tipoPagoStr);
            } catch (IllegalArgumentException e) {
                return "Error: Tipo de pago inválido. Valores permitidos: contado, cuotas";
            }

            if (tipoPago == TipoPago.cuotas && cantCuotas < 2) {
                return "Error: Para pago en cuotas, la cantidad de cuotas debe ser mayor o igual a 2.";
            }

            // 2. Verificar disponibilidad de insumos (Receta)
            DRecetas receta = DRecetas.obtenerPorProducto(productoId);
            if (receta == null) {
                return "Error: El producto '" + producto.getNombre() + "' no tiene una receta registrada. No se puede fabricar.";
            }

            List<DRecetaDetalle> ingredientes = DRecetaDetalle.listarPorReceta(receta.getId());
            if (ingredientes.isEmpty()) {
                return "Error: La receta del producto está vacía. No contiene insumos.";
            }

            // Validar stock de cada insumo
            for (DRecetaDetalle ing : ingredientes) {
                DInsumos ins = DInsumos.obtenerPorId(ing.getInsumo_id());
                if (ins == null) {
                    return "Error: El insumo con ID " + ing.getInsumo_id() + " requerido por la receta no existe.";
                }
                double requerido = ing.getCantidad() * cantidad;
                if (ins.getStock_actual() < requerido) {
                    double faltante = requerido - ins.getStock_actual();
                    return "Error: Stock insuficiente de '" + ins.getNombre() + "'. Requerido: " + requerido + " " + ins.getUnidad_medida().name() + ", Disponible: " + ins.getStock_actual() + ". Faltan: " + faltante;
                }
            }

            // 3. Todo validado. Iniciar inserciones
            conn = Conexion.getConexion();
            conn.setAutoCommit(false); // Iniciar transacción manual para garantizar consistencia

            // Descontar stock de insumos y registrar movimientos
            for (DRecetaDetalle ing : ingredientes) {
                DInsumos ins = DInsumos.obtenerPorId(ing.getInsumo_id());
                double requerido = ing.getCantidad() * cantidad;
                ins.setStock_actual(ins.getStock_actual() - requerido);
                ins.modificar();

                DMovimientosInsumo mov = new DMovimientosInsumo();
                mov.setInsumo_id(ins.getId());
                mov.setTipo(TipoMovimientoInsumo.consumo);
                mov.setCantidad(-requerido);
                mov.setDescripcion("Consumo fabricación pedido de " + producto.getNombre());
                // Registraremos el pedido_id más adelante cuando lo tengamos
                mov.insertar();
            }

            // Registrar Pedido
            double total = producto.getPrecio_unitario() * cantidad;
            DPedidos pedido = new DPedidos();
            pedido.setUsuario_id(clienteId);
            pedido.setTotal(total);
            pedido.setEstado(tipoPago == TipoPago.contado ? EstadoPedido.pagado : EstadoPedido.pendiente);

            if (!pedido.insertar()) {
                conn.rollback();
                return "Error: No se pudo crear el registro del pedido.";
            }

            // Registrar Detalle de Pedido
            DDetallePedido det = new DDetallePedido();
            det.setPedido_id(pedido.getId());
            det.setProducto_id(productoId);
            det.setCantidad(cantidad);
            det.setPrecio_unitario(producto.getPrecio_unitario());

            if (!det.insertar()) {
                conn.rollback();
                return "Error: No se pudo crear el detalle del pedido.";
            }

            // Registrar Pago
            DPagos pago = new DPagos();
            pago.setPedido_id(pedido.getId());
            pago.setTipo_pago(tipoPago);

            if (!pago.insertar()) {
                conn.rollback();
                return "Error: No se pudo registrar el pago.";
            }

            // Generar Cuotas si corresponde
            if (tipoPago == TipoPago.cuotas) {
                double montoCuota = total / cantCuotas;
                long hoy = System.currentTimeMillis();
                for (int c = 1; c <= cantCuotas; c++) {
                    DCuotas cuota = new DCuotas();
                    cuota.setPago_id(pago.getId());
                    cuota.setNumero_cuota(c);
                    cuota.setMonto_cuota(montoCuota);
                    // Vencimiento semanal: 7 días, 14 días, 21 días...
                    long vencimientoMs = hoy + (c * 7L * 24L * 60L * 60L * 1000L);
                    cuota.setFecha_vencimiento(new Date(vencimientoMs));
                    cuota.setFecha_pago(null);
                    cuota.setPagado(false);

                    if (!cuota.insertar()) {
                        conn.rollback();
                        return "Error: No se pudo generar el plan de cuotas.";
                    }
                }
            }

            // Enlazar el pedido_id en los movimientos de insumos creados anteriormente
            // (Mediante una sentencia de actualización simple)
            String updateMovSql = "UPDATE movimientos_insumo SET pedido_id = ? WHERE pedido_id IS NULL AND tipo = 'consumo'";
            try (PreparedStatement ps = conn.prepareStatement(updateMovSql)) {
                ps.setInt(1, pedido.getId());
                ps.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            return "Éxito: Pedido registrado correctamente con ID " + pedido.getId() + " por un total de " + total + " Bs.";

        } catch (NumberFormatException e) {
            return "Error: Los IDs, cantidad y cuotas deben ser numéricos enteros.";
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
            }
            return "Error en BD durante la transacción: " + e.getMessage();
        }
    }

    // CU6-02: Ver Detalle de Pedido
    // Params: ["PedidoID"]
    public static String verDetallePedido(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU6-02[\"PedidoID\"]";
        }
        try {
            int pedidoId = Integer.parseInt(parametros.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) {
                return "Error: No existe el pedido con ID " + pedidoId;
            }

            DUsuarios cliente = DUsuarios.obtenerPorId(pedido.getUsuario_id());
            String clienteNombre = cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : "Desconocido";

            List<DDetallePedido> detalles = DDetallePedido.listarPorPedido(pedidoId);
            DPagos pagoFetched = DPagos.obtenerPorPedido(pedidoId);
            String tipoPago = pagoFetched != null ? pagoFetched.getTipo_pago().name() : "N/A";

            StringBuilder sb = new StringBuilder();
            sb.append("Detalle del Pedido ID: ").append(pedidoId).append("\n");
            sb.append("Fecha: ").append(pedido.getFecha()).append(" | Cliente: ").append(clienteNombre).append("\n");
            sb.append("Estado del Pedido: ").append(pedido.getEstado().name().toUpperCase()).append(" | Tipo de Pago: ").append(tipoPago.toUpperCase()).append("\n");
            sb.append("--------------------------------------------------------------------------\n");
            sb.append("Producto | Cantidad | Precio Unitario | Subtotal\n");
            sb.append("--------------------------------------------------------------------------\n");

            for (DDetallePedido det : detalles) {
                DProductos prod = DProductos.obtenerPorId(det.getProducto_id());
                String prodNombre = prod != null ? prod.getNombre() : "Producto #" + det.getProducto_id();
                double sub = det.getCantidad() * det.getPrecio_unitario();
                sb.append(prodNombre).append(" | ")
                  .append(det.getCantidad()).append(" | ")
                  .append(det.getPrecio_unitario()).append(" Bs | ")
                  .append(sub).append(" Bs\n");
            }
            sb.append("--------------------------------------------------------------------------\n");
            sb.append("TOTAL DEL PEDIDO: ").append(pedido.getTotal()).append(" Bs\n");

            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID del pedido debe ser entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU6-03: Listar Pedidos
    public static String listarPedidos() {
        try {
            List<DPedidos> lista = DPedidos.listar();
            if (lista.isEmpty()) {
                return "No hay pedidos registrados en el sistema.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ID | Fecha | Cliente | Total | Estado | Tipo Pago\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (DPedidos p : lista) {
                DUsuarios c = DUsuarios.obtenerPorId(p.getUsuario_id());
                String clienteNombre = c != null ? c.getNombre() + " " + c.getApellido() : "Desconocido";

                DPagos pago = DPagos.obtenerPorPedido(p.getId());
                String tipoPago = pago != null ? pago.getTipo_pago().name() : "N/A";

                sb.append(p.getId()).append(" | ")
                  .append(p.getFecha()).append(" | ")
                  .append(clienteNombre).append(" | ")
                  .append(p.getTotal()).append(" Bs | ")
                  .append(p.getEstado().name().toUpperCase()).append(" | ")
                  .append(tipoPago.toUpperCase()).append("\n");
            }
            return sb.toString();
        } catch (SQLException e) {
            return "Error al listar pedidos: " + e.getMessage();
        }
    }

    // CU6-04: Cambiar Estado de Pedido
    // Params: ["PedidoID", "Estado"]
    // Estado: pendiente, pagado, entregado, cancelado
    public static String cambiarEstadoPedido(List<String> parametros) {
        if (parametros.size() < 2) {
            return "Error: Faltan parámetros. Uso: CU6-04[\"PedidoID\",\"Estado\"]";
        }
        try {
            int pedidoId = Integer.parseInt(parametros.get(0).trim());
            String estadoStr = parametros.get(1).trim().toLowerCase();

            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) {
                return "Error: No existe el pedido con ID " + pedidoId;
            }

            EstadoPedido nuevoEstado;
            try {
                nuevoEstado = EstadoPedido.valueOf(estadoStr);
            } catch (IllegalArgumentException e) {
                return "Error: Estado inválido. Valores permitidos: pendiente, pagado, entregado, cancelado";
            }

            // Si se cancela por aquí, redirigir a cancelarPedido para revertir stock
            if (nuevoEstado == EstadoPedido.cancelado) {
                return cancelarPedido(parametros);
            }

            pedido.setEstado(nuevoEstado);

            if (pedido.modificar()) {
                return "Éxito: Estado del pedido ID " + pedidoId + " cambiado a " + nuevoEstado.name().toUpperCase() + ".";
            } else {
                return "Error: No se pudo cambiar el estado del pedido.";
            }

        } catch (NumberFormatException e) {
            return "Error: El ID del pedido debe ser un entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU6-05: Cancelar Pedido (Revertir Stock)
    // Params: ["PedidoID"]
    public static String cancelarPedido(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU6-05[\"PedidoID\"]";
        }
        Connection conn = null;
        try {
            int pedidoId = Integer.parseInt(parametros.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) {
                return "Error: No existe el pedido con ID " + pedidoId;
            }

            if (pedido.getEstado() == EstadoPedido.cancelado) {
                return "Advertencia: El pedido ID " + pedidoId + " ya se encuentra cancelado.";
            }

            conn = Conexion.getConexion();
            conn.setAutoCommit(false);

            // Revertir stock de insumos: Devolver materia prima al inventario
            List<DDetallePedido> detalles = DDetallePedido.listarPorPedido(pedidoId);
            for (DDetallePedido det : detalles) {
                DRecetas receta = DRecetas.obtenerPorProducto(det.getProducto_id());
                if (receta != null) {
                    List<DRecetaDetalle> ingredientes = DRecetaDetalle.listarPorReceta(receta.getId());
                    for (DRecetaDetalle ing : ingredientes) {
                        DInsumos ins = DInsumos.obtenerPorId(ing.getInsumo_id());
                        if (ins != null) {
                            double revertido = ing.getCantidad() * det.getCantidad();
                            ins.setStock_actual(ins.getStock_actual() + revertido);
                            ins.modificar();

                            // Movimiento de tipo ajuste (corrección)
                            DMovimientosInsumo mov = new DMovimientosInsumo();
                            mov.setInsumo_id(ins.getId());
                            mov.setTipo(TipoMovimientoInsumo.ajuste);
                            mov.setCantidad(revertido);
                            mov.setDescripcion("Reversión stock por cancelación de pedido ID " + pedidoId);
                            mov.setPedido_id(pedidoId);
                            mov.insertar();
                        }
                    }
                }
            }

            pedido.setEstado(EstadoPedido.cancelado);
            if (pedido.modificar()) {
                conn.commit();
                conn.setAutoCommit(true);
                return "Éxito: Pedido ID " + pedidoId + " cancelado. Insumos devueltos al almacén.";
            } else {
                conn.rollback();
                return "Error: No se pudo cancelar el pedido.";
            }

        } catch (NumberFormatException e) {
            return "Error: El ID del pedido debe ser un entero.";
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException rollbackEx) { /* Ignorar */ }
            }
            return "Error en BD al cancelar: " + e.getMessage();
        }
    }

    // CU7-03: Registrar Pago de una Cuota
    // Params: ["CuotaID"]
    public static String registrarPagoCuota(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU7-03[\"CuotaID\"]";
        }
        try {
            int cuotaId = Integer.parseInt(parametros.get(0).trim());

            // Buscar cuota en la BD (como no hay obtenerPorId nativo en DCuotas, listamos y filtramos, o lo hacemos directo)
            // Wait, does DCuotas have obtenerPorId? Let's check DCuotas.java. No, it doesn't have it, but we can write a SQL query directly or filter.
            // Let's execute raw SQL to fetch, modify and save, which is much faster and cleaner!
            String selectSql = "SELECT * FROM cuotas WHERE id = ?";
            String updateSql = "UPDATE cuotas SET pagado = true, fecha_pago = CURRENT_DATE WHERE id = ?";

            try (Connection conn = Conexion.getConexion()) {
                int pagoId = -1;
                boolean yaPagada = false;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setInt(1, cuotaId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            pagoId = rs.getInt("pago_id");
                            yaPagada = rs.getBoolean("pagado");
                        } else {
                            return "Error: No existe la cuota con ID " + cuotaId;
                        }
                    }
                }

                if (yaPagada) {
                    return "Advertencia: La cuota ID " + cuotaId + " ya se encuentra pagada.";
                }

                // Pagar cuota
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, cuotaId);
                    if (ps.executeUpdate() == 0) {
                        return "Error: No se pudo procesar el pago de la cuota.";
                    }
                }

                // Verificar si todas las cuotas asociadas al pago_id se encuentran pagadas
                String checkAllSql = "SELECT COUNT(*) FROM cuotas WHERE pago_id = ? AND pagado = false";
                int pendientes = 0;
                try (PreparedStatement ps = conn.prepareStatement(checkAllSql)) {
                    ps.setInt(1, pagoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            pendientes = rs.getInt(1);
                        }
                    }
                }

                if (pendientes == 0) {
                    // Obtener el pedido_id asociado al pago_id
                    String getPedSql = "SELECT pedido_id FROM pagos WHERE id = ?";
                    int pedidoId = -1;
                    try (PreparedStatement ps = conn.prepareStatement(getPedSql)) {
                        ps.setInt(1, pagoId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                pedidoId = rs.getInt(1);
                            }
                        }
                    }

                    if (pedidoId != -1) {
                        // Cambiar estado del pedido a 'pagado'
                        DPedidos ped = DPedidos.obtenerPorId(pedidoId);
                        if (ped != null) {
                            ped.setEstado(EstadoPedido.pagado);
                            ped.modificar();
                        }
                    }
                }

                return "Éxito: Cuota ID " + cuotaId + " pagada correctamente." + (pendientes == 0 ? " ¡El pedido ha sido cancelado en su totalidad (estado PAGADO)!" : "");
            }
        } catch (NumberFormatException e) {
            return "Error: El ID de la cuota debe ser entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU7-04: Ver Estado de Cuotas de un Pedido
    // Params: ["PedidoID"]
    public static String verEstadoCuotas(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU7-04[\"PedidoID\"]";
        }
        try {
            int pedidoId = Integer.parseInt(parametros.get(0).trim());
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) {
                return "Error: No existe el pedido con ID " + pedidoId;
            }

            DPagos pago = DPagos.obtenerPorPedido(pedidoId);
            if (pago == null) {
                return "Error: No hay registro de pagos para el pedido ID " + pedidoId;
            }

            if (pago.getTipo_pago() == TipoPago.contado) {
                return "El pago del pedido ID " + pedidoId + " se realizó al CONTADO. No posee cuotas.";
            }

            List<DCuotas> cuotas = DCuotas.listarPorPago(pago.getId());
            if (cuotas.isEmpty()) {
                return "No se encontraron registros de cuotas para este pedido.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Plan de Cuotas para el Pedido ID: ").append(pedidoId).append(" (Total: ").append(pedido.getTotal()).append(" Bs)\n");
            sb.append("Cuota ID | Nro. Cuota | Monto | Fecha Vencimiento | Fecha Pago | Pagado\n");
            sb.append("--------------------------------------------------------------------------\n");

            for (DCuotas c : cuotas) {
                String fPago = c.getFecha_pago() != null ? c.getFecha_pago().toString() : "Pendiente";
                sb.append(c.getId()).append(" | ")
                  .append(c.getNumero_cuota()).append(" | ")
                  .append(c.getMonto_cuota()).append(" Bs | ")
                  .append(c.getFecha_vencimiento()).append(" | ")
                  .append(fPago).append(" | ")
                  .append(c.isPagado() ? "SÍ" : "NO").append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID del pedido debe ser entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU7-05: Ver Cuotas Vencidas
    public static String verCuotasVencidas() {
        String sql = "SELECT c.id AS cuota_id, c.numero_cuota, c.monto_cuota, c.fecha_vencimiento, " +
                     "u.nombre || ' ' || u.apellido AS cliente, u.telefono, p.id AS pedido_id " +
                     "FROM cuotas c " +
                     "JOIN pagos pg ON pg.id = c.pago_id " +
                     "JOIN pedidos p ON p.id = pg.pedido_id " +
                     "JOIN usuarios u ON u.id = p.usuario_id " +
                     "WHERE c.pagado = false AND c.fecha_vencimiento < CURRENT_DATE " +
                     "ORDER BY c.fecha_vencimiento ASC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder sb = new StringBuilder();
            sb.append("Listado de Cuotas Vencidas Sin Pagar:\n");
            sb.append("Cuota ID | Vencimiento | Monto | Pedido ID | Cliente | Teléfono\n");
            sb.append("--------------------------------------------------------------------------\n");
            boolean found = false;
            while (rs.next()) {
                sb.append(rs.getInt("cuota_id")).append(" | ")
                  .append(rs.getDate("fecha_vencimiento")).append(" | ")
                  .append(rs.getDouble("monto_cuota")).append(" Bs | ")
                  .append(rs.getInt("pedido_id")).append(" | ")
                  .append(rs.getString("cliente")).append(" | ")
                  .append(rs.getString("telefono")).append("\n");
                found = true;
            }
            return found ? sb.toString() : "¡Excelente! No hay cuotas vencidas pendientes de pago.";

        } catch (SQLException e) {
            return "Error al verificar cuotas vencidas: " + e.getMessage();
        }
    }
}

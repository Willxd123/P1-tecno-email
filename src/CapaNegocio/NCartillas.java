package CapaNegocio;

import CapaDatos.Conexion;
import CapaDatos.DUsuarios;
import CapaDatos.DCartillas;
import CapaDatos.DProductos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NCartillas {

    // CU4-01: Ver Cartilla de un Cliente
    // Params: ["ClienteID"]
    public static String verCartillaCliente(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta el parámetro ClienteID. Uso: CU4-01[\"ClienteID\"] o VERCAR[\"ClienteID\"]";
        }

        try {
            int clienteId = Integer.parseInt(parametros.get(0).trim());
            DUsuarios cliente = DUsuarios.obtenerPorId(clienteId);
            if (cliente == null) {
                return "Error: El cliente con ID " + clienteId + " no está registrado.";
            }

            // Obtener todas las cartillas del cliente
            List<DCartillas> cartillas = DCartillas.listarPorUsuario(clienteId);
            if (cartillas.isEmpty()) {
                return "El cliente " + cliente.getNombre() + " " + cliente.getApellido() + " aún no registra ninguna cartilla en el sistema.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("CLIENTE: ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append(" (ID: ").append(clienteId).append(")\n");
            
            for (DCartillas cart : cartillas) {
                sb.append("==========================================\n");
                sb.append("CARTILLA ID: ").append(cart.getId()).append("\n");
                sb.append("ESTADO: ").append(cart.getEstado()).append("\n");
                sb.append("FECHA INICIO: ").append(cart.getFecha_inicio()).append("\n");
                sb.append("FECHA FIN: ").append(cart.getFecha_fin() != null ? cart.getFecha_fin().toString() : "N/A").append("\n");
                sb.append("FECHA CANJE: ").append(cart.getFecha_canje() != null ? cart.getFecha_canje().toString() : "N/A").append("\n");

                if (cart.getChifon_regalo_id() != null) {
                    DProductos premio = DProductos.obtenerPorId(cart.getChifon_regalo_id());
                    sb.append("PREMIO SABOR: ").append(premio != null ? premio.getNombre() : "Desconocido").append("\n");
                    sb.append("PREMIO ENVASE DEVUELTO: ").append(cart.isEnvase_regalo_devuelto() ? "SÍ" : "NO").append("\n");
                } else {
                    sb.append("PREMIO SABOR: N/A\n");
                    sb.append("PREMIO ENVASE DEVUELTO: N/A\n");
                }

                // Calcular acumulados de chifones y envases para esta cartilla
                int chifonesComprados = 0;
                int envasesDevueltos = 0;

                String sqlChifonesCount = "SELECT COALESCE(SUM(dp.cantidad), 0) " +
                                         "FROM detalle_pedido dp " +
                                         "JOIN pedidos p ON dp.pedido_id = p.id " +
                                         "JOIN productos pr ON dp.producto_id = pr.id " +
                                         "WHERE p.cartilla_id = ? " +
                                         "  AND (p.estado = 'pagado' OR p.estado = 'entregado') " +
                                         "  AND (pr.nombre ILIKE '%chifón%' OR pr.nombre ILIKE '%chifon%') " +
                                         "  AND dp.precio_unitario > 0";

                String sqlEnvasesCount = "SELECT COALESCE(SUM(pe.cantidad_devuelta), 0) " +
                                        "FROM pedido_envase pe " +
                                        "JOIN pedidos p ON pe.pedido_origen_id = p.id " +
                                        "WHERE p.cartilla_id = ?";

                try (Connection conn = Conexion.getConexion()) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlChifonesCount)) {
                        ps.setInt(1, cart.getId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                chifonesComprados = rs.getInt(1);
                            }
                        }
                    }
                    try (PreparedStatement ps = conn.prepareStatement(sqlEnvasesCount)) {
                        ps.setInt(1, cart.getId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                envasesDevueltos = rs.getInt(1);
                            }
                        }
                    }
                }

                sb.append("ACUMULADO CHIFONES: ").append(chifonesComprados).append("/10\n");
                sb.append("ACUMULADO ENVASES: ").append(envasesDevueltos).append("/10\n");

                // Obtener pedidos de esta cartilla
                sb.append("--- PEDIDOS EN ESTA CARTILLA ---\n");
                String sqlPedidos = "SELECT p.id, p.fecha, p.total, p.estado " +
                                    "FROM pedidos p " +
                                    "WHERE p.cartilla_id = ? " +
                                    "ORDER BY p.fecha DESC";

                try (Connection conn = Conexion.getConexion();
                     PreparedStatement ps = conn.prepareStatement(sqlPedidos)) {
                    ps.setInt(1, cart.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean tienePedidos = false;
                        while (rs.next()) {
                            int pedId = rs.getInt("id");
                            sb.append("PEDIDO: ").append(pedId).append(" | ")
                              .append(rs.getTimestamp("fecha")).append(" | ")
                              .append(rs.getDouble("total")).append(" Bs | ")
                              .append(rs.getString("estado").toUpperCase()).append(" | ");

                            // Obtener productos de este pedido
                            String sqlDetalles = "SELECT dp.cantidad, pr.nombre " +
                                                 "FROM detalle_pedido dp " +
                                                 "JOIN productos pr ON dp.producto_id = pr.id " +
                                                 "WHERE dp.pedido_id = ?";
                            try (PreparedStatement psDet = conn.prepareStatement(sqlDetalles)) {
                                psDet.setInt(1, pedId);
                                try (ResultSet rsDet = psDet.executeQuery()) {
                                    StringBuilder prodSb = new StringBuilder();
                                    while (rsDet.next()) {
                                        if (prodSb.length() > 0) prodSb.append(", ");
                                        prodSb.append(rsDet.getInt("cantidad")).append("x ").append(rsDet.getString("nombre"));
                                    }
                                    sb.append(prodSb.toString()).append("\n");
                                }
                            }
                            tienePedidos = true;
                        }
                        if (!tienePedidos) {
                            sb.append("Ninguno\n");
                        }
                    }
                }

                // Obtener control de envases prestados y devueltos en esta cartilla
                sb.append("--- CONTROL ENVS EN ESTA CARTILLA ---\n");
                String sqlEnvases = "SELECT pe.id, e.nombre AS tipo_envase, pe.cantidad_prestada, pe.cantidad_devuelta, pe.fecha_devolucion " +
                                    "FROM pedido_envase pe " +
                                    "JOIN envases e ON pe.envase_id = e.id " +
                                    "JOIN pedidos p ON pe.pedido_origen_id = p.id " +
                                    "WHERE p.cartilla_id = ? " +
                                    "ORDER BY pe.id DESC";

                try (Connection conn = Conexion.getConexion();
                     PreparedStatement ps = conn.prepareStatement(sqlEnvases)) {
                    ps.setInt(1, cart.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean tieneEnvases = false;
                        while (rs.next()) {
                            String fDevolucion = rs.getTimestamp("fecha_devolucion") != null ? rs.getTimestamp("fecha_devolucion").toString() : "N/A";
                            int prestados = rs.getInt("cantidad_prestada");
                            int devueltos = rs.getInt("cantidad_devuelta");
                            String estadoEnv;
                            if (devueltos == prestados) estadoEnv = "DEVUELTO";
                            else if (devueltos > 0) estadoEnv = "PARCIAL";
                            else estadoEnv = "PENDIENTE";

                            sb.append("ENVASE: ").append(rs.getString("tipo_envase")).append(" | Prestados: ")
                              .append(prestados).append(" | Devueltos: ")
                              .append(devueltos).append(" | Fecha Dev: ")
                              .append(fDevolucion).append(" | Estado: ")
                              .append(estadoEnv).append("\n");
                            tieneEnvases = true;
                        }
                        if (!tieneEnvases) {
                            sb.append("Ninguno\n");
                        }
                    }
                }
            }

            return sb.toString();

        } catch (NumberFormatException e) {
            return "Error: El ID del cliente debe ser un valor entero.";
        } catch (SQLException e) {
            return "Error en BD al consultar la cartilla: " + e.getMessage();
        }
    }
}

package CapaNegocio;

import CapaDatos.Conexion;
import CapaDatos.DUsuarios;

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

            String sql = "SELECT " +
                    "    p.id                        AS pedido_id, " +
                    "    p.fecha                     AS fecha_pedido, " +
                    "    p.total                     AS monto_pedido, " +
                    "    p.estado                    AS estado_pedido, " +
                    "    COALESCE(e.nombre, 'Ninguno') AS tipo_envase, " +
                    "    COALESCE(pe.cantidad_prestada, 0) AS cantidad_prestada, " +
                    "    COALESCE(pe.cantidad_devuelta, 0) AS cantidad_devuelta, " +
                    "    pe.fecha_devolucion, " +
                    "    CASE " +
                    "        WHEN pe.pedido_origen_id IS NULL THEN 'sin envase' " +
                    "        WHEN pe.cantidad_devuelta = pe.cantidad_prestada THEN 'devuelto' " +
                    "        WHEN pe.cantidad_devuelta > 0 THEN 'devuelto parcial' " +
                    "        ELSE 'pendiente' " +
                    "    END                         AS estado_envase " +
                    "FROM pedidos p " +
                    "LEFT JOIN pedido_envase pe ON pe.pedido_origen_id = p.id " +
                    "LEFT JOIN envases e        ON e.id = pe.envase_id " +
                    "WHERE p.usuario_id = ? " +
                    "ORDER BY p.fecha DESC";

            try (Connection conn = Conexion.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, clienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cartilla Digital de Cliente: ").append(cliente.getNombre()).append(" ").append(cliente.getApellido()).append(" (ID: ").append(clienteId).append(")\n");
                    sb.append("Pedido ID | Fecha | Monto | Estado Pedido | Envase | Prestados | Devueltos | Estado Envase\n");
                    sb.append("--------------------------------------------------------------------------\n");

                    boolean tienePedidos = false;
                    while (rs.next()) {
                        String fDevolucion = rs.getTimestamp("fecha_devolucion") != null ? rs.getTimestamp("fecha_devolucion").toString() : "N/A";
                        sb.append(rs.getInt("pedido_id")).append(" | ")
                          .append(rs.getTimestamp("fecha_pedido")).append(" | ")
                          .append(rs.getDouble("monto_pedido")).append(" Bs | ")
                          .append(rs.getString("estado_pedido").toUpperCase()).append(" | ")
                          .append(rs.getString("tipo_envase")).append(" | ")
                          .append(rs.getInt("cantidad_prestada")).append(" | ")
                          .append(rs.getInt("cantidad_devuelta")).append(" | ")
                          .append(rs.getString("estado_envase").toUpperCase()).append("\n");
                        tienePedidos = true;
                    }

                    if (!tienePedidos) {
                        return "El cliente " + cliente.getNombre() + " " + cliente.getApellido() + " aún no registra ningún pedido en el sistema.";
                    }

                    return sb.toString();
                }
            }

        } catch (NumberFormatException e) {
            return "Error: El ID del cliente debe ser un valor entero.";
        } catch (SQLException e) {
            return "Error en BD al consultar la cartilla: " + e.getMessage();
        }
    }
}

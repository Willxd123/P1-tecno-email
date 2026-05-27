package CapaNegocio;

import CapaDatos.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class NReportes {

    // CU8-01: Reporte de Ventas por Período
    // Params: ["FechaInicio", "FechaFin"] (ej: ["2026-05-01", "2026-05-31"])
    public static String reporteVentas(List<String> parametros) {
        if (parametros.size() < 2) {
            return "Error: Faltan parámetros. Uso: CU8-01[\"FechaInicio\",\"FechaFin\"]";
        }
        String inicio = parametros.get(0).trim() + " 00:00:00";
        String fin = parametros.get(1).trim() + " 23:59:59";

        String sqlTotales = "SELECT COUNT(id) AS total_pedidos, COALESCE(SUM(total), 0) AS total_monto " +
                "FROM pedidos WHERE fecha >= ?::timestamp AND fecha <= ?::timestamp AND estado <> 'cancelado'";

        String sqlDetalle = "SELECT pr.nombre AS producto, SUM(dp.cantidad) AS unidades_vendidas, " +
                "SUM(dp.cantidad * dp.precio_unitario) AS monto_total " +
                "FROM detalle_pedido dp " +
                "JOIN pedidos p ON p.id = dp.pedido_id " +
                "JOIN productos pr ON pr.id = dp.producto_id " +
                "WHERE p.fecha >= ?::timestamp AND p.fecha <= ?::timestamp AND p.estado <> 'cancelado' " +
                "GROUP BY pr.nombre " +
                "ORDER BY unidades_vendidas DESC";

        try (Connection conn = Conexion.getConexion()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Ventas del ").append(parametros.get(0)).append(" al ").append(parametros.get(1)).append("\n");

            // 1. Totales
            try (PreparedStatement ps = conn.prepareStatement(sqlTotales)) {
                ps.setString(1, inicio);
                ps.setString(2, fin);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sb.append("Total de Pedidos Realizados: ").append(rs.getInt("total_pedidos")).append(" | ")
                          .append("Monto Total Facturado: ").append(rs.getDouble("total_monto")).append(" Bs\n");
                    }
                }
            }

            sb.append("--------------------------------------------------------------------------\n");
            sb.append("Producto | Unidades Vendidas | Monto Total\n");
            sb.append("--------------------------------------------------------------------------\n");

            // 2. Detalle de productos
            try (PreparedStatement ps = conn.prepareStatement(sqlDetalle)) {
                ps.setString(1, inicio);
                ps.setString(2, fin);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        sb.append(rs.getString("producto")).append(" | ")
                          .append(rs.getInt("unidades_vendidas")).append(" | ")
                          .append(rs.getDouble("monto_total")).append(" Bs\n");
                        found = true;
                    }
                    if (!found) {
                        sb.append("No se registraron ventas de productos en este período.\n");
                    }
                }
            }

            return sb.toString();
        } catch (SQLException e) {
            return "Error en BD al generar reporte de ventas: " + e.getMessage();
        }
    }

    // CU8-02: Reporte de Ingresos Contado vs Crédito
    // Params: ["FechaInicio", "FechaFin"]
    public static String reporteContadoVsCredito(List<String> parametros) {
        if (parametros.size() < 2) {
            return "Error: Faltan parámetros. Uso: CU8-02[\"FechaInicio\",\"FechaFin\"]";
        }
        String inicio = parametros.get(0).trim() + " 00:00:00";
        String fin = parametros.get(1).trim() + " 23:59:59";

        String sql = "SELECT pg.tipo_pago, COUNT(p.id) AS total_pedidos, SUM(p.total) AS total_monto " +
                "FROM pagos pg " +
                "JOIN pedidos p ON p.id = pg.pedido_id " +
                "WHERE p.fecha >= ?::timestamp AND p.fecha <= ?::timestamp AND p.estado <> 'cancelado' " +
                "GROUP BY pg.tipo_pago";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio);
            ps.setString(2, fin);
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Reporte de Ingresos Contado vs Crédito del ").append(parametros.get(0)).append(" al ").append(parametros.get(1)).append("\n");
                sb.append("Tipo de Pago | Total Pedidos | Monto Total\n");
                sb.append("--------------------------------------------------------------------------\n");

                boolean found = false;
                double grandTotal = 0;
                while (rs.next()) {
                    String tipo = rs.getString("tipo_pago").toUpperCase();
                    double monto = rs.getDouble("total_monto");
                    sb.append(tipo).append(" | ")
                      .append(rs.getInt("total_pedidos")).append(" | ")
                      .append(monto).append(" Bs\n");
                    grandTotal += monto;
                    found = true;
                }

                if (!found) {
                    return "No se registraron ventas en este período.";
                }

                sb.append("--------------------------------------------------------------------------\n");
                sb.append("TOTAL FACTURADO: ").append(grandTotal).append(" Bs\n");
                return sb.toString();
            }
        } catch (SQLException e) {
            return "Error en BD al generar reporte contado vs crédito: " + e.getMessage();
        }
    }

    // CU8-03: Reporte de Cuotas Pendientes (Adeudado)
    // Params: none
    public static String reporteCuotasPendientes() {
        String sql = "SELECT u.id AS cliente_id, u.nombre || ' ' || u.apellido AS cliente, " +
                "COUNT(c.id) AS cuotas_pendientes, SUM(c.monto_cuota) AS total_adeudado " +
                "FROM cuotas c " +
                "JOIN pagos pg ON pg.id = c.pago_id " +
                "JOIN pedidos p ON p.id = pg.pedido_id " +
                "JOIN usuarios u ON u.id = p.usuario_id " +
                "WHERE c.pagado = false " +
                "GROUP BY u.id, u.nombre, u.apellido " +
                "ORDER BY total_adeudado DESC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder sb = new StringBuilder();
            sb.append("Reporte General de Cartera Adeudada (Créditos Pendientes)\n");
            sb.append("Cliente ID | Nombre Cliente | Cuotas Pendientes | Total Adeudado\n");
            sb.append("--------------------------------------------------------------------------\n");

            boolean found = false;
            double sumaTotal = 0;
            while (rs.next()) {
                double adeudado = rs.getDouble("total_adeudado");
                sb.append(rs.getInt("cliente_id")).append(" | ")
                  .append(rs.getString("cliente")).append(" | ")
                  .append(rs.getInt("cuotas_pendientes")).append(" | ")
                  .append(adeudado).append(" Bs\n");
                sumaTotal += adeudado;
                found = true;
            }

            if (!found) {
                return "¡Excelente! No existen saldos ni cuotas pendientes por cobrar en el sistema.";
            }

            sb.append("--------------------------------------------------------------------------\n");
            sb.append("TOTAL GENERAL POR COBRAR: ").append(sumaTotal).append(" Bs\n");
            return sb.toString();

        } catch (SQLException e) {
            return "Error en BD al generar reporte de cuotas pendientes: " + e.getMessage();
        }
    }

    // CU8-04: Reporte de Consumo de Insumos en un Período
    // Params: ["FechaInicio", "FechaFin"]
    public static String reporteConsumoInsumos(List<String> parametros) {
        if (parametros.size() < 2) {
            return "Error: Faltan parámetros. Uso: CU8-04[\"FechaInicio\",\"FechaFin\"]";
        }
        String inicio = parametros.get(0).trim() + " 00:00:00";
        String fin = parametros.get(1).trim() + " 23:59:59";

        String sql = "SELECT i.nombre AS insumo, i.unidad_medida, ABS(SUM(m.cantidad)) AS total_consumido " +
                "FROM movimientos_insumo m " +
                "JOIN insumos i ON i.id = m.insumo_id " +
                "WHERE m.tipo = 'consumo' AND m.fecha >= ?::timestamp AND m.fecha <= ?::timestamp " +
                "GROUP BY i.nombre, i.unidad_medida " +
                "ORDER BY total_consumido DESC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inicio);
            ps.setString(2, fin);
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Reporte de Consumo de Insumos (Materia Prima) del ").append(parametros.get(0)).append(" al ").append(parametros.get(1)).append("\n");
                sb.append("Insumo | Cantidad Consumida | Unidad\n");
                sb.append("--------------------------------------------------------------------------\n");

                boolean found = false;
                while (rs.next()) {
                    sb.append(rs.getString("insumo")).append(" | ")
                      .append(rs.getDouble("total_consumido")).append(" | ")
                      .append(rs.getString("unidad_medida")).append("\n");
                    found = true;
                }

                if (!found) {
                    return "No se registró consumo de insumos en este rango de fechas.";
                }

                return sb.toString();
            }
        } catch (SQLException e) {
            return "Error en BD al generar reporte de consumo de insumos: " + e.getMessage();
        }
    }

    // CU8-06: Reporte de Insumos con Stock Crítico (por debajo de stock mínimo)
    // Params: none
    public static String reporteStockCritico() {
        String sql = "SELECT id, nombre, stock_actual, stock_minimo, unidad_medida, " +
                "(stock_minimo - stock_actual) AS faltante " +
                "FROM insumos WHERE stock_actual < stock_minimo ORDER BY stock_actual ASC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Stock Crítico de Insumos (Alerta de Reposición)\n");
            sb.append("ID | Insumo | Stock Actual | Stock Mínimo | Unidad | Faltante\n");
            sb.append("--------------------------------------------------------------------------\n");

            boolean found = false;
            while (rs.next()) {
                sb.append(rs.getInt("id")).append(" | ")
                  .append(rs.getString("nombre")).append(" | ")
                  .append(rs.getDouble("stock_actual")).append(" | ")
                  .append(rs.getDouble("stock_minimo")).append(" | ")
                  .append(rs.getString("unidad_medida")).append(" | ")
                  .append(rs.getDouble("faltante")).append("\n");
                found = true;
            }

            if (!found) {
                return "¡Todo en orden! Todos los insumos cuentan con stock suficiente por encima del mínimo.";
            }

            return sb.toString();

        } catch (SQLException e) {
            return "Error en BD al generar reporte de stock crítico: " + e.getMessage();
        }
    }

    // CU8-07: Reporte de Envases Prestados
    // Params: none
    public static String reporteEnvasesPrestados() {
        String sql = "SELECT u.nombre || ' ' || u.apellido AS cliente, e.nombre AS envase, " +
                "SUM(pe.cantidad_prestada - pe.cantidad_devuelta) AS total_prestado " +
                "FROM pedido_envase pe " +
                "JOIN envases e ON e.id = pe.envase_id " +
                "JOIN pedidos p ON p.id = pe.pedido_origen_id " +
                "JOIN usuarios u ON u.id = p.usuario_id " +
                "WHERE pe.cantidad_prestada > pe.cantidad_devuelta " +
                "GROUP BY u.nombre, u.apellido, e.nombre " +
                "ORDER BY total_prestado DESC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder sb = new StringBuilder();
            sb.append("Reporte de Envases Prestados Actualmente en Posesión de Clientes\n");
            sb.append("Cliente | Tipo de Envase | Cantidad Prestada Pendiente\n");
            sb.append("--------------------------------------------------------------------------\n");

            boolean found = false;
            while (rs.next()) {
                sb.append(rs.getString("cliente")).append(" | ")
                  .append(rs.getString("envase")).append(" | ")
                  .append(rs.getInt("total_prestado")).append("\n");
                found = true;
            }

            if (!found) {
                return "No existen envases prestados pendientes de devolución en este momento.";
            }

            return sb.toString();

        } catch (SQLException e) {
            return "Error en BD al generar reporte de envases prestados: " + e.getMessage();
        }
    }
}

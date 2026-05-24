package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import CapaDatos.enums.TipoPago;

public class DPagos {
    private int id;
    private Timestamp fecha;
    private TipoPago tipo_pago;
    private int pedido_id;

    public DPagos() {}

    public DPagos(int id, Timestamp fecha, TipoPago tipo_pago, int pedido_id) {
        this.id = id;
        this.fecha = fecha;
        this.tipo_pago = tipo_pago;
        this.pedido_id = pedido_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public TipoPago getTipo_pago() { return tipo_pago; }
    public void setTipo_pago(TipoPago tipo_pago) { this.tipo_pago = tipo_pago; }
    public int getPedido_id() { return pedido_id; }
    public void setPedido_id(int pedido_id) { this.pedido_id = pedido_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO pagos (tipo_pago, pedido_id) VALUES (?::varchar, ?) RETURNING id, fecha";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.tipo_pago.name());
            ps.setInt(2, this.pedido_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.id = rs.getInt("id");
                    this.fecha = rs.getTimestamp("fecha");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean modificar() throws SQLException {
        String sql = "UPDATE pagos SET tipo_pago = ?::varchar, pedido_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.tipo_pago.name());
            ps.setInt(2, this.pedido_id);
            ps.setInt(3, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM pagos WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DPagos> listar() throws SQLException {
        List<DPagos> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DPagos(
                    rs.getInt("id"),
                    rs.getTimestamp("fecha"),
                    TipoPago.valueOf(rs.getString("tipo_pago")),
                    rs.getInt("pedido_id")
                ));
            }
        }
        return lista;
    }

    public static DPagos obtenerPorPedido(int pedidoId) throws SQLException {
        String sql = "SELECT * FROM pagos WHERE pedido_id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DPagos(
                        rs.getInt("id"),
                        rs.getTimestamp("fecha"),
                        TipoPago.valueOf(rs.getString("tipo_pago")),
                        rs.getInt("pedido_id")
                    );
                }
            }
        }
        return null;
    }
}

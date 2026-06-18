package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import CapaDatos.enums.EstadoPedido;

public class DPedidos {
    private int id;
    private Timestamp fecha;
    private EstadoPedido estado;
    private double total;
    private int usuario_id;
    private Integer cartilla_id;

    public DPedidos() {}

    public DPedidos(int id, Timestamp fecha, EstadoPedido estado, double total, int usuario_id, Integer cartilla_id) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.total = total;
        this.usuario_id = usuario_id;
        this.cartilla_id = cartilla_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public int getUsuario_id() { return usuario_id; }
    public void setUsuario_id(int usuario_id) { this.usuario_id = usuario_id; }
    public Integer getCartilla_id() { return cartilla_id; }
    public void setCartilla_id(Integer cartilla_id) { this.cartilla_id = cartilla_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO pedidos (estado, total, usuario_id, cartilla_id) VALUES (?::varchar, ?, ?, ?) RETURNING id, fecha";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.estado.name());
            ps.setDouble(2, this.total);
            ps.setInt(3, this.usuario_id);
            if (this.cartilla_id != null) {
                ps.setInt(4, this.cartilla_id);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
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
        String sql = "UPDATE pedidos SET estado = ?::varchar, total = ?, usuario_id = ?, cartilla_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.estado.name());
            ps.setDouble(2, this.total);
            ps.setInt(3, this.usuario_id);
            if (this.cartilla_id != null) {
                ps.setInt(4, this.cartilla_id);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM pedidos WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DPedidos> listar() throws SQLException {
        List<DPedidos> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedidos ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer cId = rs.getInt("cartilla_id");
                if (rs.wasNull()) cId = null;
                lista.add(new DPedidos(
                    rs.getInt("id"),
                    rs.getTimestamp("fecha"),
                    EstadoPedido.valueOf(rs.getString("estado")),
                    rs.getDouble("total"),
                    rs.getInt("usuario_id"),
                    cId
                ));
            }
        }
        return lista;
    }

    public static DPedidos obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM pedidos WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer cId = rs.getInt("cartilla_id");
                    if (rs.wasNull()) cId = null;
                    return new DPedidos(
                        rs.getInt("id"),
                        rs.getTimestamp("fecha"),
                        EstadoPedido.valueOf(rs.getString("estado")),
                        rs.getDouble("total"),
                        rs.getInt("usuario_id"),
                        cId
                    );
                }
            }
        }
        return null;
    }
}

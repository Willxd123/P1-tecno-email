package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import CapaDatos.enums.TipoMovimientoInsumo;

public class DMovimientosInsumo {
    private int id;
    private Timestamp fecha;
    private TipoMovimientoInsumo tipo;
    private double cantidad;
    private String descripcion;
    private int insumo_id;
    private Integer pedido_id; // Puede ser NULL

    public DMovimientosInsumo() {}

    public DMovimientosInsumo(int id, Timestamp fecha, TipoMovimientoInsumo tipo, double cantidad, String descripcion, int insumo_id, Integer pedido_id) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
        this.insumo_id = insumo_id;
        this.pedido_id = pedido_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }
    public TipoMovimientoInsumo getTipo() { return tipo; }
    public void setTipo(TipoMovimientoInsumo tipo) { this.tipo = tipo; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getInsumo_id() { return insumo_id; }
    public void setInsumo_id(int insumo_id) { this.insumo_id = insumo_id; }
    public Integer getPedido_id() { return pedido_id; }
    public void setPedido_id(Integer pedido_id) { this.pedido_id = pedido_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO movimientos_insumo (tipo, cantidad, descripcion, insumo_id, pedido_id) VALUES (?::varchar, ?, ?, ?, ?) RETURNING id, fecha";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.tipo.name());
            ps.setDouble(2, this.cantidad);
            ps.setString(3, this.descripcion);
            ps.setInt(4, this.insumo_id);
            if (this.pedido_id != null) {
                ps.setInt(5, this.pedido_id);
            } else {
                ps.setNull(5, Types.INTEGER);
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
        String sql = "UPDATE movimientos_insumo SET tipo = ?::varchar, cantidad = ?, descripcion = ?, insumo_id = ?, pedido_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.tipo.name());
            ps.setDouble(2, this.cantidad);
            ps.setString(3, this.descripcion);
            ps.setInt(4, this.insumo_id);
            if (this.pedido_id != null) {
                ps.setInt(5, this.pedido_id);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM movimientos_insumo WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DMovimientosInsumo> listarPorInsumo(int insumoId) throws SQLException {
        List<DMovimientosInsumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_insumo WHERE insumo_id = ? ORDER BY fecha DESC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, insumoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer pId = rs.getInt("pedido_id");
                    if (rs.wasNull()) {
                        pId = null;
                    }
                    lista.add(new DMovimientosInsumo(
                        rs.getInt("id"),
                        rs.getTimestamp("fecha"),
                        TipoMovimientoInsumo.valueOf(rs.getString("tipo")),
                        rs.getDouble("cantidad"),
                        rs.getString("descripcion"),
                        rs.getInt("insumo_id"),
                        pId
                    ));
                }
            }
        }
        return lista;
    }

    public static List<DMovimientosInsumo> listarPorPedido(int pedidoId) throws SQLException {
        List<DMovimientosInsumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_insumo WHERE pedido_id = ? ORDER BY fecha DESC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer pId = rs.getInt("pedido_id"); if (rs.wasNull()) pId = null;
                    lista.add(new DMovimientosInsumo(rs.getInt("id"), rs.getTimestamp("fecha"),
                        TipoMovimientoInsumo.valueOf(rs.getString("tipo")), rs.getDouble("cantidad"),
                        rs.getString("descripcion"), rs.getInt("insumo_id"), pId));
                }
            }
        }
        return lista;
    }

    public static List<DMovimientosInsumo> listarTodos() throws SQLException {
        List<DMovimientosInsumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos_insumo ORDER BY fecha DESC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer pId = rs.getInt("pedido_id"); if (rs.wasNull()) pId = null;
                lista.add(new DMovimientosInsumo(rs.getInt("id"), rs.getTimestamp("fecha"),
                    TipoMovimientoInsumo.valueOf(rs.getString("tipo")), rs.getDouble("cantidad"),
                    rs.getString("descripcion"), rs.getInt("insumo_id"), pId));
            }
        }
        return lista;
    }
}

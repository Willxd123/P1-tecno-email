package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DDetallePedido {
    private int id;
    private int cantidad;
    private double precio_unitario;
    private int pedido_id;
    private int producto_id;

    public DDetallePedido() {}

    public DDetallePedido(int id, int cantidad, double precio_unitario, int pedido_id, int producto_id) {
        this.id = id;
        this.cantidad = cantidad;
        this.precio_unitario = precio_unitario;
        this.pedido_id = pedido_id;
        this.producto_id = producto_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecio_unitario() { return precio_unitario; }
    public void setPrecio_unitario(double precio_unitario) { this.precio_unitario = precio_unitario; }
    public int getPedido_id() { return pedido_id; }
    public void setPedido_id(int pedido_id) { this.pedido_id = pedido_id; }
    public int getProducto_id() { return producto_id; }
    public void setProducto_id(int producto_id) { this.producto_id = producto_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO detalle_pedido (cantidad, precio_unitario, pedido_id, producto_id) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.cantidad);
            ps.setDouble(2, this.precio_unitario);
            ps.setInt(3, this.pedido_id);
            ps.setInt(4, this.producto_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.id = rs.getInt("id");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean modificar() throws SQLException {
        String sql = "UPDATE detalle_pedido SET cantidad = ?, precio_unitario = ?, pedido_id = ?, producto_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.cantidad);
            ps.setDouble(2, this.precio_unitario);
            ps.setInt(3, this.pedido_id);
            ps.setInt(4, this.producto_id);
            ps.setInt(5, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM detalle_pedido WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DDetallePedido> listar() throws SQLException {
        List<DDetallePedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_pedido ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DDetallePedido(
                    rs.getInt("id"),
                    rs.getInt("cantidad"),
                    rs.getDouble("precio_unitario"),
                    rs.getInt("pedido_id"),
                    rs.getInt("producto_id")
                ));
            }
        }
        return lista;
    }

    public static List<DDetallePedido> listarPorPedido(int pedidoId) throws SQLException {
        List<DDetallePedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM detalle_pedido WHERE pedido_id = ? ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DDetallePedido(
                        rs.getInt("id"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getInt("pedido_id"),
                        rs.getInt("producto_id")
                    ));
                }
            }
        }
        return lista;
    }
}

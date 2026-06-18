package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DProductos {
    private int id;
    private String nombre;
    private String descripcion;
    private double precio_unitario;
    private boolean disponible;
    private Integer categoria_producto_id;

    public DProductos() {}

    public DProductos(int id, String nombre, String descripcion, double precio_unitario, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio_unitario = precio_unitario;
        this.disponible = disponible;
        this.categoria_producto_id = null;
    }

    public DProductos(int id, String nombre, String descripcion, double precio_unitario, boolean disponible, Integer categoria_producto_id) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio_unitario = precio_unitario;
        this.disponible = disponible;
        this.categoria_producto_id = categoria_producto_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio_unitario() { return precio_unitario; }
    public void setPrecio_unitario(double precio_unitario) { this.precio_unitario = precio_unitario; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public Integer getCategoria_producto_id() { return categoria_producto_id; }
    public void setCategoria_producto_id(Integer categoria_producto_id) { this.categoria_producto_id = categoria_producto_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO productos (nombre, descripcion, precio_unitario, disponible, categoria_producto_id) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.descripcion);
            ps.setDouble(3, this.precio_unitario);
            ps.setBoolean(4, this.disponible);
            if (this.categoria_producto_id != null) {
                ps.setInt(5, this.categoria_producto_id);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
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
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, precio_unitario = ?, disponible = ?, categoria_producto_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.descripcion);
            ps.setDouble(3, this.precio_unitario);
            ps.setBoolean(4, this.disponible);
            if (this.categoria_producto_id != null) {
                ps.setInt(5, this.categoria_producto_id);
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setInt(6, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DProductos> listar() throws SQLException {
        List<DProductos> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int catId = rs.getInt("categoria_producto_id");
                Integer categoriaId = rs.wasNull() ? null : catId;
                lista.add(new DProductos(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_unitario"),
                    rs.getBoolean("disponible"),
                    categoriaId
                ));
            }
        }
        return lista;
    }

    public static DProductos obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM productos WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int catId = rs.getInt("categoria_producto_id");
                    Integer categoriaId = rs.wasNull() ? null : catId;
                    return new DProductos(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio_unitario"),
                        rs.getBoolean("disponible"),
                        categoriaId
                    );
                }
            }
        }
        return null;
    }
}

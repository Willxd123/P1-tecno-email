package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DRecetas {
    private int id;
    private String nombre;
    private String descripcion;
    private Timestamp creado_en;
    private int producto_id;

    public DRecetas() {}

    public DRecetas(int id, String nombre, String descripcion, Timestamp creado_en, int producto_id) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creado_en = creado_en;
        this.producto_id = producto_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Timestamp getCreado_en() { return creado_en; }
    public void setCreado_en(Timestamp creado_en) { this.creado_en = creado_en; }
    public int getProducto_id() { return producto_id; }
    public void setProducto_id(int producto_id) { this.producto_id = producto_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO recetas (nombre, descripcion, producto_id) VALUES (?, ?, ?) RETURNING id, creado_en";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.descripcion);
            ps.setInt(3, this.producto_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.id = rs.getInt("id");
                    this.creado_en = rs.getTimestamp("creado_en");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean modificar() throws SQLException {
        String sql = "UPDATE recetas SET nombre = ?, descripcion = ?, producto_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.descripcion);
            ps.setInt(3, this.producto_id);
            ps.setInt(4, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM recetas WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DRecetas> listar() throws SQLException {
        List<DRecetas> lista = new ArrayList<>();
        String sql = "SELECT * FROM recetas ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DRecetas(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getTimestamp("creado_en"),
                    rs.getInt("producto_id")
                ));
            }
        }
        return lista;
    }

    public static DRecetas obtenerPorProducto(int productoId) throws SQLException {
        String sql = "SELECT * FROM recetas WHERE producto_id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DRecetas(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getTimestamp("creado_en"),
                        rs.getInt("producto_id")
                    );
                }
            }
        }
        return null;
    }
}

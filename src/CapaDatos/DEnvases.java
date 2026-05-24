package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DEnvases {
    private int id;
    private String nombre;
    private String descripcion;
    private int stock_total;
    private int stock_disponible;

    public DEnvases() {}

    public DEnvases(int id, String nombre, String descripcion, int stock_total, int stock_disponible) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.stock_total = stock_total;
        this.stock_disponible = stock_disponible;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getStock_total() { return stock_total; }
    public void setStock_total(int stock_total) { this.stock_total = stock_total; }
    public int getStock_disponible() { return stock_disponible; }
    public void setStock_disponible(int stock_disponible) { this.stock_disponible = stock_disponible; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO envases (nombre, descripcion, stock_total, stock_disponible) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.descripcion);
            ps.setInt(3, this.stock_total);
            ps.setInt(4, this.stock_disponible);
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
        String sql = "UPDATE envases SET nombre = ?, descripcion = ?, stock_total = ?, stock_disponible = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.descripcion);
            ps.setInt(3, this.stock_total);
            ps.setInt(4, this.stock_disponible);
            ps.setInt(5, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM envases WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DEnvases> listar() throws SQLException {
        List<DEnvases> lista = new ArrayList<>();
        String sql = "SELECT * FROM envases ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DEnvases(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getInt("stock_total"),
                    rs.getInt("stock_disponible")
                ));
            }
        }
        return lista;
    }

    public static DEnvases obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM envases WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DEnvases(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("stock_total"),
                        rs.getInt("stock_disponible")
                    );
                }
            }
        }
        return null;
    }

    public static DEnvases obtenerPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM envases WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DEnvases(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("stock_total"),
                        rs.getInt("stock_disponible")
                    );
                }
            }
        }
        return null;
    }
}

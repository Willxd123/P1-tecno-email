package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DCategoriaProducto {
    private int id;
    private String nombre;

    public DCategoriaProducto() {}

    public DCategoriaProducto(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO categoria_producto (nombre) VALUES (?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
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
        String sql = "UPDATE categoria_producto SET nombre = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setInt(2, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM categoria_producto WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DCategoriaProducto> listar() throws SQLException {
        List<DCategoriaProducto> lista = new ArrayList<>();
        String sql = "SELECT * FROM categoria_producto ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DCategoriaProducto(
                    rs.getInt("id"),
                    rs.getString("nombre")
                ));
            }
        }
        return lista;
    }

    public static DCategoriaProducto obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM categoria_producto WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DCategoriaProducto(
                        rs.getInt("id"),
                        rs.getString("nombre")
                    );
                }
            }
        }
        return null;
    }

    public static DCategoriaProducto obtenerPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM categoria_producto WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?))";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DCategoriaProducto(
                        rs.getInt("id"),
                        rs.getString("nombre")
                    );
                }
            }
        }
        return null;
    }
}

package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DUsuarios {
    private int id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String password;
    private int rol_id;
    private boolean activo;
    private String email;

    public DUsuarios() {
    }

    public DUsuarios(int id, String nombre, String apellido, String telefono, String email, String password,
            int rol_id, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.password = password;
        this.rol_id = rol_id;
        this.activo = activo;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getpassword() {
        return password;
    }

    public void setpassword(String password) {
        this.password = password;
    }

    public int getRol_id() {
        return rol_id;
    }

    public void setRol_id(int rol_id) {
        this.rol_id = rol_id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, apellido, telefono, email, password, rol_id, activo) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.apellido);
            ps.setString(3, this.telefono);
            ps.setString(4, this.email);
            ps.setString(5, this.password);
            ps.setInt(6, this.rol_id);
            ps.setBoolean(7, this.activo);
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
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, telefono = ?, email = ?, password = ?, rol_id = ?, activo = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.apellido);
            ps.setString(3, this.telefono);
            ps.setString(4, this.email);
            ps.setString(5, this.password);
            ps.setInt(6, this.rol_id);
            ps.setBoolean(7, this.activo);
            ps.setInt(8, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DUsuarios> listar() throws SQLException {
        List<DUsuarios> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DUsuarios(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("rol_id"),
                        rs.getBoolean("activo")));
            }
        }
        return lista;
    }

    public static DUsuarios obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DUsuarios(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("telefono"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getInt("rol_id"),
                            rs.getBoolean("activo"));
                }
            }
        }
        return null;
    }

    public static DUsuarios obtenerPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection conn = Conexion.getConexion();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DUsuarios(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("telefono"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getInt("rol_id"),
                            rs.getBoolean("activo"));
                }
            }
        }
        return null;
    }
}

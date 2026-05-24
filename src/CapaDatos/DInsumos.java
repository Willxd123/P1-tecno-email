package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import CapaDatos.enums.UnidadMedida;

public class DInsumos {
    private int id;
    private String nombre;
    private UnidadMedida unidad_medida;
    private double stock_actual;
    private double stock_minimo;
    private double costo_unitario;

    public DInsumos() {}

    public DInsumos(int id, String nombre, UnidadMedida unidad_medida, double stock_actual, double stock_minimo, double costo_unitario) {
        this.id = id;
        this.nombre = nombre;
        this.unidad_medida = unidad_medida;
        this.stock_actual = stock_actual;
        this.stock_minimo = stock_minimo;
        this.costo_unitario = costo_unitario;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public UnidadMedida getUnidad_medida() { return unidad_medida; }
    public void setUnidad_medida(UnidadMedida unidad_medida) { this.unidad_medida = unidad_medida; }
    public double getStock_actual() { return stock_actual; }
    public void setStock_actual(double stock_actual) { this.stock_actual = stock_actual; }
    public double getStock_minimo() { return stock_minimo; }
    public void setStock_minimo(double stock_minimo) { this.stock_minimo = stock_minimo; }
    public double getCosto_unitario() { return costo_unitario; }
    public void setCosto_unitario(double costo_unitario) { this.costo_unitario = costo_unitario; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO insumos (nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario) VALUES (?, ?::varchar, ?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.unidad_medida.name());
            ps.setDouble(3, this.stock_actual);
            ps.setDouble(4, this.stock_minimo);
            ps.setDouble(5, this.costo_unitario);
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
        String sql = "UPDATE insumos SET nombre = ?, unidad_medida = ?::varchar, stock_actual = ?, stock_minimo = ?, costo_unitario = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.nombre);
            ps.setString(2, this.unidad_medida.name());
            ps.setDouble(3, this.stock_actual);
            ps.setDouble(4, this.stock_minimo);
            ps.setDouble(5, this.costo_unitario);
            ps.setInt(6, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM insumos WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DInsumos> listar() throws SQLException {
        List<DInsumos> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new DInsumos(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    UnidadMedida.valueOf(rs.getString("unidad_medida")),
                    rs.getDouble("stock_actual"),
                    rs.getDouble("stock_minimo"),
                    rs.getDouble("costo_unitario")
                ));
            }
        }
        return lista;
    }

    public static DInsumos obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM insumos WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DInsumos(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        UnidadMedida.valueOf(rs.getString("unidad_medida")),
                        rs.getDouble("stock_actual"),
                        rs.getDouble("stock_minimo"),
                        rs.getDouble("costo_unitario")
                    );
                }
            }
        }
        return null;
    }
}

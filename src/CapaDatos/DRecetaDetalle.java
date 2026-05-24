package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DRecetaDetalle {
    private int id;
    private double cantidad;
    private int receta_id;
    private int insumo_id;

    public DRecetaDetalle() {}

    public DRecetaDetalle(int id, double cantidad, int receta_id, int insumo_id) {
        this.id = id;
        this.cantidad = cantidad;
        this.receta_id = receta_id;
        this.insumo_id = insumo_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }
    public int getReceta_id() { return receta_id; }
    public void setReceta_id(int receta_id) { this.receta_id = receta_id; }
    public int getInsumo_id() { return insumo_id; }
    public void setInsumo_id(int insumo_id) { this.insumo_id = insumo_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO receta_detalle (cantidad, receta_id, insumo_id) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, this.cantidad);
            ps.setInt(2, this.receta_id);
            ps.setInt(3, this.insumo_id);
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
        String sql = "UPDATE receta_detalle SET cantidad = ?, receta_id = ?, insumo_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, this.cantidad);
            ps.setInt(2, this.receta_id);
            ps.setInt(3, this.insumo_id);
            ps.setInt(4, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM receta_detalle WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DRecetaDetalle> listarPorReceta(int recetaId) throws SQLException {
        List<DRecetaDetalle> lista = new ArrayList<>();
        String sql = "SELECT * FROM receta_detalle WHERE receta_id = ? ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recetaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DRecetaDetalle(
                        rs.getInt("id"),
                        rs.getDouble("cantidad"),
                        rs.getInt("receta_id"),
                        rs.getInt("insumo_id")
                    ));
                }
            }
        }
        return lista;
    }
}

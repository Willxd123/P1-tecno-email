package CapaDatos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DCuotas {
    private int id;
    private int numero_cuota;
    private double monto_cuota;
    private Date fecha_vencimiento;
    private Date fecha_pago;
    private boolean pagado;
    private int pago_id;

    public DCuotas() {}

    public DCuotas(int id, int numero_cuota, double monto_cuota, Date fecha_vencimiento, Date fecha_pago, boolean pagado, int pago_id) {
        this.id = id;
        this.numero_cuota = numero_cuota;
        this.monto_cuota = monto_cuota;
        this.fecha_vencimiento = fecha_vencimiento;
        this.fecha_pago = fecha_pago;
        this.pagado = pagado;
        this.pago_id = pago_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNumero_cuota() { return numero_cuota; }
    public void setNumero_cuota(int numero_cuota) { this.numero_cuota = numero_cuota; }
    public double getMonto_cuota() { return monto_cuota; }
    public void setMonto_cuota(double monto_cuota) { this.monto_cuota = monto_cuota; }
    public Date getFecha_vencimiento() { return fecha_vencimiento; }
    public void setFecha_vencimiento(Date fecha_vencimiento) { this.fecha_vencimiento = fecha_vencimiento; }
    public Date getFecha_pago() { return fecha_pago; }
    public void setFecha_pago(Date fecha_pago) { this.fecha_pago = fecha_pago; }
    public boolean isPagado() { return pagado; }
    public void setPagado(boolean pagado) { this.pagado = pagado; }
    public int getPago_id() { return pago_id; }
    public void setPago_id(int pago_id) { this.pago_id = pago_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO cuotas (numero_cuota, monto_cuota, fecha_vencimiento, fecha_pago, pagado, pago_id) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.numero_cuota);
            ps.setDouble(2, this.monto_cuota);
            ps.setDate(3, this.fecha_vencimiento);
            ps.setDate(4, this.fecha_pago);
            ps.setBoolean(5, this.pagado);
            ps.setInt(6, this.pago_id);
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
        String sql = "UPDATE cuotas SET numero_cuota = ?, monto_cuota = ?, fecha_vencimiento = ?, fecha_pago = ?, pagado = ?, pago_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.numero_cuota);
            ps.setDouble(2, this.monto_cuota);
            ps.setDate(3, this.fecha_vencimiento);
            ps.setDate(4, this.fecha_pago);
            ps.setBoolean(5, this.pagado);
            ps.setInt(6, this.pago_id);
            ps.setInt(7, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM cuotas WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DCuotas> listarPorPago(int pagoId) throws SQLException {
        List<DCuotas> lista = new ArrayList<>();
        String sql = "SELECT * FROM cuotas WHERE pago_id = ? ORDER BY numero_cuota ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pagoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DCuotas(
                        rs.getInt("id"),
                        rs.getInt("numero_cuota"),
                        rs.getDouble("monto_cuota"),
                        rs.getDate("fecha_vencimiento"),
                        rs.getDate("fecha_pago"),
                        rs.getBoolean("pagado"),
                        rs.getInt("pago_id")
                    ));
                }
            }
        }
        return lista;
    }
}

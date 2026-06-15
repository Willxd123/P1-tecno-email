package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DCartillas {
    private int id;
    private int usuario_id;
    private String estado; // 'activa', 'completada', 'canjeada'
    private Timestamp fecha_inicio;
    private Timestamp fecha_fin;
    private Integer chifon_regalo_id;
    private Timestamp fecha_canje;
    private boolean envase_regalo_devuelto;

    public DCartillas() {}

    public DCartillas(int id, int usuario_id, String estado, Timestamp fecha_inicio, Timestamp fecha_fin, Integer chifon_regalo_id, Timestamp fecha_canje, boolean envase_regalo_devuelto) {
        this.id = id;
        this.usuario_id = usuario_id;
        this.estado = estado;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
        this.chifon_regalo_id = chifon_regalo_id;
        this.fecha_canje = fecha_canje;
        this.envase_regalo_devuelto = envase_regalo_devuelto;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuario_id() { return usuario_id; }
    public void setUsuario_id(int usuario_id) { this.usuario_id = usuario_id; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Timestamp getFecha_inicio() { return fecha_inicio; }
    public void setFecha_inicio(Timestamp fecha_inicio) { this.fecha_inicio = fecha_inicio; }
    public Timestamp getFecha_fin() { return fecha_fin; }
    public void setFecha_fin(Timestamp fecha_fin) { this.fecha_fin = fecha_fin; }
    public Integer getChifon_regalo_id() { return chifon_regalo_id; }
    public void setChifon_regalo_id(Integer chifon_regalo_id) { this.chifon_regalo_id = chifon_regalo_id; }
    public Timestamp getFecha_canje() { return fecha_canje; }
    public void setFecha_canje(Timestamp fecha_canje) { this.fecha_canje = fecha_canje; }
    public boolean isEnvase_regalo_devuelto() { return envase_regalo_devuelto; }
    public void setEnvase_regalo_devuelto(boolean envase_regalo_devuelto) { this.envase_regalo_devuelto = envase_regalo_devuelto; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO cartillas (usuario_id, estado, fecha_fin, chifon_regalo_id, fecha_canje, envase_regalo_devuelto) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, fecha_inicio";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.usuario_id);
            ps.setString(2, this.estado);
            ps.setTimestamp(3, this.fecha_fin);
            if (this.chifon_regalo_id != null) {
                ps.setInt(4, this.chifon_regalo_id);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setTimestamp(5, this.fecha_canje);
            ps.setBoolean(6, this.envase_regalo_devuelto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.id = rs.getInt("id");
                    this.fecha_inicio = rs.getTimestamp("fecha_inicio");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean modificar() throws SQLException {
        String sql = "UPDATE cartillas SET usuario_id = ?, estado = ?, fecha_inicio = ?, fecha_fin = ?, " +
                     "chifon_regalo_id = ?, fecha_canje = ?, envase_regalo_devuelto = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.usuario_id);
            ps.setString(2, this.estado);
            ps.setTimestamp(3, this.fecha_inicio);
            ps.setTimestamp(4, this.fecha_fin);
            if (this.chifon_regalo_id != null) {
                ps.setInt(5, this.chifon_regalo_id);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setTimestamp(6, this.fecha_canje);
            ps.setBoolean(7, this.envase_regalo_devuelto);
            ps.setInt(8, this.id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM cartillas WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static DCartillas obtenerActivaPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT * FROM cartillas WHERE usuario_id = ? AND estado = 'activa' LIMIT 1";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer chifonId = rs.getInt("chifon_regalo_id");
                    if (rs.wasNull()) chifonId = null;
                    return new DCartillas(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("estado"),
                        rs.getTimestamp("fecha_inicio"),
                        rs.getTimestamp("fecha_fin"),
                        chifonId,
                        rs.getTimestamp("fecha_canje"),
                        rs.getBoolean("envase_regalo_devuelto")
                    );
                }
            }
        }
        return null;
    }

    public static List<DCartillas> listarPorUsuario(int usuarioId) throws SQLException {
        List<DCartillas> lista = new ArrayList<>();
        String sql = "SELECT * FROM cartillas WHERE usuario_id = ? ORDER BY fecha_inicio DESC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer chifonId = rs.getInt("chifon_regalo_id");
                    if (rs.wasNull()) chifonId = null;
                    lista.add(new DCartillas(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("estado"),
                        rs.getTimestamp("fecha_inicio"),
                        rs.getTimestamp("fecha_fin"),
                        chifonId,
                        rs.getTimestamp("fecha_canje"),
                        rs.getBoolean("envase_regalo_devuelto")
                    ));
                }
            }
        }
        return lista;
    }

    public static DCartillas obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM cartillas WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer chifonId = rs.getInt("chifon_regalo_id");
                    if (rs.wasNull()) chifonId = null;
                    return new DCartillas(
                        rs.getInt("id"),
                        rs.getInt("usuario_id"),
                        rs.getString("estado"),
                        rs.getTimestamp("fecha_inicio"),
                        rs.getTimestamp("fecha_fin"),
                        chifonId,
                        rs.getTimestamp("fecha_canje"),
                        rs.getBoolean("envase_regalo_devuelto")
                    );
                }
            }
        }
        return null;
    }
}

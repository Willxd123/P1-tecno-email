package CapaDatos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DPedidoEnvase {
    private int id;
    private int cantidad_prestada;
    private int cantidad_devuelta;
    private Timestamp fecha_devolucion;
    private int pedido_origen_id;
    private Integer pedido_devolucion_id; // Nullable
    private int envase_id;

    public DPedidoEnvase() {}

    public DPedidoEnvase(int id, int cantidad_prestada, int cantidad_devuelta, Timestamp fecha_devolucion, int pedido_origen_id, Integer pedido_devolucion_id, int envase_id) {
        this.id = id;
        this.cantidad_prestada = cantidad_prestada;
        this.cantidad_devuelta = cantidad_devuelta;
        this.fecha_devolucion = fecha_devolucion;
        this.pedido_origen_id = pedido_origen_id;
        this.pedido_devolucion_id = pedido_devolucion_id;
        this.envase_id = envase_id;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCantidad_prestada() { return cantidad_prestada; }
    public void setCantidad_prestada(int cantidad_prestada) { this.cantidad_prestada = cantidad_prestada; }
    public int getCantidad_devuelta() { return cantidad_devuelta; }
    public void setCantidad_devuelta(int cantidad_devuelta) { this.cantidad_devuelta = cantidad_devuelta; }
    public Timestamp getFecha_devolucion() { return fecha_devolucion; }
    public void setFecha_devolucion(Timestamp fecha_devolucion) { this.fecha_devolucion = fecha_devolucion; }
    public int getPedido_origen_id() { return pedido_origen_id; }
    public void setPedido_origen_id(int pedido_origen_id) { this.pedido_origen_id = pedido_origen_id; }
    public Integer getPedido_devolucion_id() { return pedido_devolucion_id; }
    public void setPedido_devolucion_id(Integer pedido_devolucion_id) { this.pedido_devolucion_id = pedido_devolucion_id; }
    public int getEnvase_id() { return envase_id; }
    public void setEnvase_id(int envase_id) { this.envase_id = envase_id; }

    // ==========================================
    // OPERACIONES CRUD CON LA BASE DE DATOS
    // ==========================================

    public boolean insertar() throws SQLException {
        String sql = "INSERT INTO pedido_envase (cantidad_prestada, cantidad_devuelta, fecha_devolucion, pedido_origen_id, pedido_devolucion_id, envase_id) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.cantidad_prestada);
            ps.setInt(2, this.cantidad_devuelta);
            ps.setTimestamp(3, this.fecha_devolucion);
            ps.setInt(4, this.pedido_origen_id);
            if (this.pedido_devolucion_id != null) {
                ps.setInt(5, this.pedido_devolucion_id);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, this.envase_id);

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
        String sql = "UPDATE pedido_envase SET cantidad_prestada = ?, cantidad_devuelta = ?, fecha_devolucion = ?, pedido_origen_id = ?, pedido_devolucion_id = ?, envase_id = ? WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.cantidad_prestada);
            ps.setInt(2, this.cantidad_devuelta);
            ps.setTimestamp(3, this.fecha_devolucion);
            ps.setInt(4, this.pedido_origen_id);
            if (this.pedido_devolucion_id != null) {
                ps.setInt(5, this.pedido_devolucion_id);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, this.envase_id);
            ps.setInt(7, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar() throws SQLException {
        String sql = "DELETE FROM pedido_envase WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<DPedidoEnvase> listar() throws SQLException {
        List<DPedidoEnvase> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido_envase ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer devId = rs.getInt("pedido_devolucion_id");
                if (rs.wasNull()) {
                    devId = null;
                }
                lista.add(new DPedidoEnvase(
                    rs.getInt("id"),
                    rs.getInt("cantidad_prestada"),
                    rs.getInt("cantidad_devuelta"),
                    rs.getTimestamp("fecha_devolucion"),
                    rs.getInt("pedido_origen_id"),
                    devId,
                    rs.getInt("envase_id")
                ));
            }
        }
        return lista;
    }

    public static DPedidoEnvase obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM pedido_envase WHERE id = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer devId = rs.getInt("pedido_devolucion_id");
                    if (rs.wasNull()) {
                        devId = null;
                    }
                    return new DPedidoEnvase(
                        rs.getInt("id"),
                        rs.getInt("cantidad_prestada"),
                        rs.getInt("cantidad_devuelta"),
                        rs.getTimestamp("fecha_devolucion"),
                        rs.getInt("pedido_origen_id"),
                        devId,
                        rs.getInt("envase_id")
                    );
                }
            }
        }
        return null;
    }

    public static List<DPedidoEnvase> listarPorPedidoOrigen(int pedidoOrigenId) throws SQLException {
        List<DPedidoEnvase> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido_envase WHERE pedido_origen_id = ? ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoOrigenId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer devId = rs.getInt("pedido_devolucion_id");
                    if (rs.wasNull()) {
                        devId = null;
                    }
                    lista.add(new DPedidoEnvase(
                        rs.getInt("id"),
                        rs.getInt("cantidad_prestada"),
                        rs.getInt("cantidad_devuelta"),
                        rs.getTimestamp("fecha_devolucion"),
                        rs.getInt("pedido_origen_id"),
                        devId,
                        rs.getInt("envase_id")
                    ));
                }
            }
        }
        return lista;
    }

    public static List<DPedidoEnvase> listarPorPedidoDevolucion(int pedidoDevolucionId) throws SQLException {
        List<DPedidoEnvase> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido_envase WHERE pedido_devolucion_id = ? ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pedidoDevolucionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer devId = rs.getInt("pedido_devolucion_id");
                    if (rs.wasNull()) {
                        devId = null;
                    }
                    lista.add(new DPedidoEnvase(
                        rs.getInt("id"),
                        rs.getInt("cantidad_prestada"),
                        rs.getInt("cantidad_devuelta"),
                        rs.getTimestamp("fecha_devolucion"),
                        rs.getInt("pedido_origen_id"),
                        devId,
                        rs.getInt("envase_id")
                    ));
                }
            }
        }
        return lista;
    }

    public static List<DPedidoEnvase> listarPendientes() throws SQLException {
        List<DPedidoEnvase> lista = new ArrayList<>();
        String sql = "SELECT * FROM pedido_envase WHERE cantidad_devuelta < cantidad_prestada ORDER BY id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer devId = rs.getInt("pedido_devolucion_id"); if (rs.wasNull()) devId = null;
                lista.add(new DPedidoEnvase(rs.getInt("id"), rs.getInt("cantidad_prestada"), rs.getInt("cantidad_devuelta"),
                    rs.getTimestamp("fecha_devolucion"), rs.getInt("pedido_origen_id"), devId, rs.getInt("envase_id")));
            }
        }
        return lista;
    }

    public static List<DPedidoEnvase> listarPorUsuario(int usuarioId) throws SQLException {
        List<DPedidoEnvase> lista = new ArrayList<>();
        String sql = "SELECT pe.* FROM pedido_envase pe JOIN pedidos p ON pe.pedido_origen_id = p.id WHERE p.usuario_id = ? ORDER BY pe.id ASC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer devId = rs.getInt("pedido_devolucion_id"); if (rs.wasNull()) devId = null;
                    lista.add(new DPedidoEnvase(rs.getInt("id"), rs.getInt("cantidad_prestada"), rs.getInt("cantidad_devuelta"),
                        rs.getTimestamp("fecha_devolucion"), rs.getInt("pedido_origen_id"), devId, rs.getInt("envase_id")));
                }
            }
        }
        return lista;
    }
}

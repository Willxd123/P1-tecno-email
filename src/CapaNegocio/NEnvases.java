package CapaNegocio;

import CapaDatos.Conexion;
import CapaDatos.DEnvases;
import CapaDatos.DPedidoEnvase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class NEnvases {

    // CU3-01: Registrar Tipo de Envase
    // Params: ["Nombre", "Descripcion", "StockTotal"]
    public static String registrarTipoEnvase(List<String> parametros) {
        if (parametros.size() < 3) {
            return "Error: Faltan parámetros. Uso: CU3-01[\"Nombre\",\"Descripcion\",\"StockTotal\"]";
        }
        try {
            String nombre = parametros.get(0).trim();
            String descripcion = parametros.get(1).trim();
            int stockTotal = Integer.parseInt(parametros.get(2).trim());

            if (nombre.isEmpty()) {
                return "Error: El nombre del envase no puede estar vacío.";
            }
            if (stockTotal <= 0) {
                return "Error: El stock total debe ser mayor a 0.";
            }

            DEnvases env = new DEnvases();
            env.setNombre(nombre);
            env.setDescripcion(descripcion);
            env.setStock_total(stockTotal);
            env.setStock_disponible(stockTotal); // Disponible = Total inicialmente

            if (env.insertar()) {
                return "Éxito: Tipo de envase '" + nombre + "' registrado con ID " + env.getId();
            } else {
                return "Error: No se pudo registrar el tipo de envase.";
            }
        } catch (NumberFormatException e) {
            return "Error: El stock total debe ser un número entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU3-02: Editar Tipo de Envase
    // Params: ["ID", "Nombre", "Descripcion", "StockTotal"]
    public static String editarTipoEnvase(List<String> parametros) {
        if (parametros.size() < 4) {
            return "Error: Faltan parámetros. Uso: CU3-02[\"ID\",\"Nombre\",\"Descripcion\",\"StockTotal\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            String nombre = parametros.get(1).trim();
            String descripcion = parametros.get(2).trim();
            int stockTotal = Integer.parseInt(parametros.get(3).trim());

            DEnvases env = DEnvases.obtenerPorId(id);
            if (env == null) {
                return "Error: No existe el envase con ID " + id;
            }

            if (nombre.isEmpty()) {
                return "Error: El nombre del envase no puede estar vacío.";
            }
            if (stockTotal <= 0) {
                return "Error: El stock total debe ser mayor a 0.";
            }

            // Calcular nuevo disponible (no puede superar total y no puede ser negativo)
            int prestados = env.getStock_total() - env.getStock_disponible();
            int nuevoDisponible = stockTotal - prestados;
            if (nuevoDisponible < 0) {
                return "Error: No se puede reducir el stock total a " + stockTotal + " porque actualmente hay " + prestados + " envases prestados.";
            }

            env.setNombre(nombre);
            env.setDescripcion(descripcion);
            env.setStock_total(stockTotal);
            env.setStock_disponible(nuevoDisponible);

            if (env.modificar()) {
                return "Éxito: Envase ID " + id + " modificado correctamente.";
            } else {
                return "Error: No se pudo modificar el envase.";
            }
        } catch (NumberFormatException e) {
            return "Error: ID y Stock Total deben ser enteros.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU3-03: Ver Stock de Envases
    public static String listarEnvases() {
        try {
            List<DEnvases> lista = DEnvases.listar();
            if (lista.isEmpty()) {
                return "No hay tipos de envases registrados en el catálogo.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre | Descripción | Stock Total | Stock Disponible | En Préstamo\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (DEnvases env : lista) {
                int prestados = env.getStock_total() - env.getStock_disponible();
                sb.append(env.getId()).append(" | ")
                  .append(env.getNombre()).append(" | ")
                  .append(env.getDescripcion()).append(" | ")
                  .append(env.getStock_total()).append(" | ")
                  .append(env.getStock_disponible()).append(" | ")
                  .append(prestados).append("\n");
            }
            return sb.toString();
        } catch (SQLException e) {
            return "Error al listar envases: " + e.getMessage();
        }
    }

    // CU3-04: Registrar Préstamo de Envases
    // Params: ["PedidoOrigenID", "EnvaseID", "CantidadPrestada"]
    public static String registrarPrestamo(List<String> parametros) {
        if (parametros.size() < 3) {
            return "Error: Faltan parámetros. Uso: CU3-04[\"PedidoOrigenID\",\"EnvaseID\",\"CantidadPrestada\"]";
        }
        try {
            int pedidoOrigenId = Integer.parseInt(parametros.get(0).trim());
            int envaseId = Integer.parseInt(parametros.get(1).trim());
            int cantidadPrestada = Integer.parseInt(parametros.get(2).trim());

            if (cantidadPrestada <= 0) {
                return "Error: La cantidad prestada debe ser mayor a 0.";
            }

            DEnvases env = DEnvases.obtenerPorId(envaseId);
            if (env == null) {
                return "Error: No existe el tipo de envase con ID " + envaseId;
            }

            if (env.getStock_disponible() < cantidadPrestada) {
                return "Error: Stock disponible insuficiente. Disponible: " + env.getStock_disponible() + ", Solicitado: " + cantidadPrestada;
            }

            // Actualizar stock disponible
            env.setStock_disponible(env.getStock_disponible() - cantidadPrestada);

            if (env.modificar()) {
                DPedidoEnvase pe = new DPedidoEnvase();
                pe.setPedido_origen_id(pedidoOrigenId);
                pe.setEnvase_id(envaseId);
                pe.setCantidad_prestada(cantidadPrestada);
                pe.setCantidad_devuelta(0);
                pe.setFecha_devolucion(null);
                pe.setPedido_devolucion_id(null);

                if (pe.insertar()) {
                    return "Éxito: Préstamo registrado correctamente. " + cantidadPrestada + " unidades de '" + env.getNombre() + "' prestadas.";
                } else {
                    // Revertir stock
                    env.setStock_disponible(env.getStock_disponible() + cantidadPrestada);
                    env.modificar();
                    return "Error: No se pudo registrar el préstamo del envase.";
                }
            } else {
                return "Error: No se pudo actualizar el inventario del envase.";
            }
        } catch (NumberFormatException e) {
            return "Error: Todos los parámetros deben ser numéricos enteros.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU3-05: Registrar Devolución de Envases
    // Params: ["PedidoOrigenID", "PedidoDevolucionID", "EnvaseID", "CantidadDevuelta"]
    public static String registrarDevolucion(List<String> parametros) {
        if (parametros.size() < 4) {
            return "Error: Faltan parámetros. Uso: CU3-05[\"PedidoOrigenID\",\"PedidoDevolucionID\",\"EnvaseID\",\"CantidadDevuelta\"]";
        }
        try {
            int pedidoOrigenId = Integer.parseInt(parametros.get(0).trim());
            int pedidoDevolucionId = Integer.parseInt(parametros.get(1).trim());
            int envaseId = Integer.parseInt(parametros.get(2).trim());
            int cantidadDevuelta = Integer.parseInt(parametros.get(3).trim());

            if (cantidadDevuelta <= 0) {
                return "Error: La cantidad a devolver debe ser mayor a 0.";
            }

            // Buscar el préstamo
            List<DPedidoEnvase> prestamos = DPedidoEnvase.listarPorPedidoOrigen(pedidoOrigenId);
            DPedidoEnvase prestamo = null;
            for (DPedidoEnvase p : prestamos) {
                if (p.getEnvase_id() == envaseId) {
                    prestamo = p;
                    break;
                }
            }

            if (prestamo == null) {
                return "Error: No se encontró ningún préstamo para el Pedido de Origen " + pedidoOrigenId + " y Envase " + envaseId;
            }

            int cupoRestante = prestamo.getCantidad_prestada() - prestamo.getCantidad_devuelta();
            if (cantidadDevuelta > cupoRestante) {
                return "Error: La cantidad devuelta supera lo pendiente. Pendiente: " + cupoRestante + ", Intento Devolución: " + cantidadDevuelta;
            }

            DEnvases env = DEnvases.obtenerPorId(envaseId);
            if (env == null) {
                return "Error: El envase ya no está registrado en el catálogo.";
            }

            // Actualizar el préstamo
            prestamo.setCantidad_devuelta(prestamo.getCantidad_devuelta() + cantidadDevuelta);
            prestamo.setPedido_devolucion_id(pedidoDevolucionId);
            prestamo.setFecha_devolucion(new Timestamp(System.currentTimeMillis()));

            if (prestamo.modificar()) {
                // Devolver stock al almacén
                env.setStock_disponible(env.getStock_disponible() + cantidadDevuelta);
                env.modificar();

                // Actualizar progreso de la cartilla
                try {
                    CapaDatos.DPedidos ped = CapaDatos.DPedidos.obtenerPorId(pedidoOrigenId);
                    if (ped != null) {
                        NPedidos.actualizarProgresoCartilla(ped.getUsuario_id(), ped.getCartilla_id());
                    }
                } catch (Exception e) {
                    System.err.println("[NEnvases] Error al actualizar progreso de cartilla tras devolución: " + e.getMessage());
                }

                return "Éxito: Devolución registrada correctamente. " + cantidadDevuelta + " unidades de '" + env.getNombre() + "' devueltas.";
            } else {
                return "Error: No se pudo registrar la devolución.";
            }

        } catch (NumberFormatException e) {
            return "Error: Todos los parámetros deben ser enteros.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU3-06: Ver Envases Pendientes por Cliente
    public static String verEnvasesPendientes() {
        String sql = "SELECT u.nombre, u.apellido, u.telefono, e.nombre AS tipo_envase, " +
                     "SUM(pe.cantidad_prestada - pe.cantidad_devuelta) AS envases_pendientes, " +
                     "MIN(p.fecha) AS desde_fecha " +
                     "FROM pedido_envase pe " +
                     "JOIN pedidos p ON p.id = pe.pedido_origen_id " +
                     "JOIN usuarios u ON u.id = p.usuario_id " +
                     "JOIN envases e ON e.id = pe.envase_id " +
                     "WHERE pe.cantidad_devuelta < pe.cantidad_prestada " +
                     "GROUP BY u.id, u.nombre, u.apellido, u.telefono, e.nombre " +
                     "ORDER BY envases_pendientes DESC";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder sb = new StringBuilder();
            sb.append("Envases Pendientes por Devolver por Cliente:\n");
            sb.append("Cliente | Teléfono | Tipo de Envase | Cantidad Pendiente | Desde Fecha\n");
            sb.append("--------------------------------------------------------------------------\n");
            boolean found = false;
            while (rs.next()) {
                sb.append(rs.getString("nombre")).append(" ").append(rs.getString("apellido")).append(" | ")
                  .append(rs.getString("telefono")).append(" | ")
                  .append(rs.getString("tipo_envase")).append(" | ")
                  .append(rs.getInt("envases_pendientes")).append(" | ")
                  .append(rs.getTimestamp("desde_fecha")).append("\n");
                found = true;
            }
            return found ? sb.toString() : "No hay envases pendientes de devolución en todo el sistema.";

        } catch (SQLException e) {
            return "Error al verificar envases pendientes: " + e.getMessage();
        }
    }

    // CU3-07: Ver Historial de Envases de un Cliente
    // Params: ["ClienteID"]
    public static String verHistorialEnvasesCliente(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU3-07[\"ClienteID\"]";
        }
        String sql = "SELECT p.id AS pedido_origen_id, e.nombre AS tipo_envase, " +
                     "pe.cantidad_prestada, pe.cantidad_devuelta, pe.fecha_devolucion, pe.pedido_devolucion_id " +
                     "FROM pedido_envase pe " +
                     "JOIN pedidos p ON p.id = pe.pedido_origen_id " +
                     "JOIN envases e ON e.id = pe.envase_id " +
                     "WHERE p.usuario_id = ? " +
                     "ORDER BY p.fecha DESC";

        try {
            int clienteId = Integer.parseInt(parametros.get(0).trim());

            try (Connection conn = Conexion.getConexion();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, clienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Historial de Envases de Cliente ID: ").append(clienteId).append("\n");
                    sb.append("Ped. Origen | Tipo de Envase | Prestado | Devuelto | Fecha Devolución | Ped. Devolución\n");
                    sb.append("---------------------------------------------------------------------------------------------\n");
                    boolean found = false;
                    while (rs.next()) {
                        Timestamp fDev = rs.getTimestamp("fecha_devolucion");
                        String fDevStr = fDev != null ? fDev.toString() : "Pendiente";
                        int devId = rs.getInt("pedido_devolucion_id");
                        String devIdStr = rs.wasNull() ? "N/A" : String.valueOf(devId);

                        sb.append(rs.getInt("pedido_origen_id")).append(" | ")
                          .append(rs.getString("tipo_envase")).append(" | ")
                          .append(rs.getInt("cantidad_prestada")).append(" | ")
                          .append(rs.getInt("cantidad_devuelta")).append(" | ")
                          .append(fDevStr).append(" | ")
                          .append(devIdStr).append("\n");
                        found = true;
                    }
                    return found ? sb.toString() : "El cliente no tiene préstamos ni devoluciones de envases.";
                }
            }
        } catch (NumberFormatException e) {
            return "Error: El ID del cliente debe ser un entero.";
        } catch (SQLException e) {
            return "Error al obtener historial: " + e.getMessage();
        }
    }
}

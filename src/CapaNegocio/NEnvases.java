package CapaNegocio;

import CapaDatos.DEnvases;
import CapaDatos.DPedidoEnvase;
import CapaDatos.DPedidos;
import CapaDatos.DUsuarios;

import java.sql.Timestamp;
import java.util.List;

public class NEnvases {

    // CU3_01: REGENV["nombre","descripcion","stock_total"]
    public static String registrarTipoEnvase(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: nombre, descripcion, stock_total.";
        try {
            String nombre = p.get(0).trim();
            String desc = p.get(1).trim();
            int stockTotal = Integer.parseInt(p.get(2).trim());
            if (nombre.isEmpty()) return "Error: El nombre del envase no puede estar vacío.";
            if (stockTotal <= 0) return "Error: El stock total debe ser mayor a 0.";
            DEnvases env = new DEnvases();
            env.setNombre(nombre);
            env.setDescripcion(desc);
            env.setStock_total(stockTotal);
            env.setStock_disponible(stockTotal);
            if (env.insertar()) {
                return "Éxito: Envase '" + nombre + "' registrado con ID " + env.getId() + ". Stock total: " + stockTotal + ".";
            }
            return "Error: No se pudo registrar el envase.";
        } catch (NumberFormatException e) {
            return "Error: El stock total debe ser un número entero.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU3_02: EDTENV["id","nombre","descripcion"]
    public static String editarTipoEnvase(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: id, nombre, descripcion.";
        try {
            int id = Integer.parseInt(p.get(0).trim());
            String nombre = p.get(1).trim();
            String desc = p.get(2).trim();
            if (nombre.isEmpty()) return "Error: El nombre no puede estar vacío.";
            DEnvases env = DEnvases.obtenerPorId(id);
            if (env == null) return "Error: No existe un envase con ID " + id + ".";
            env.setNombre(nombre);
            env.setDescripcion(desc);
            if (env.modificar()) return "Éxito: Envase ID " + id + " actualizado correctamente.";
            return "Error: No se pudo actualizar el envase.";
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU3_03: LISENV
    public static String verStockEnvases(List<String> p) {
        try {
            List<DEnvases> lista = DEnvases.listar();
            if (lista.isEmpty()) return "No hay envases registrados.";
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre | Stock Total | Disponible | Prestados\n");
            sb.append("---\n");
            for (DEnvases e : lista) {
                int prestados = e.getStock_total() - e.getStock_disponible();
                sb.append(e.getId()).append(" | ")
                  .append(e.getNombre()).append(" | ")
                  .append(e.getStock_total()).append(" | ")
                  .append(e.getStock_disponible()).append(" | ")
                  .append(prestados).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar envases: " + e.getMessage();
        }
    }

    // CU3_04: PRESENV["pedido_id","envase_id","cantidad"]
    public static String registrarPrestamo(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: pedido_id, envase_id, cantidad.";
        try {
            int pedidoId = Integer.parseInt(p.get(0).trim());
            int envaseId = Integer.parseInt(p.get(1).trim());
            int cantidad = Integer.parseInt(p.get(2).trim());
            if (cantidad <= 0) return "Error: La cantidad debe ser mayor a 0.";
            DPedidos pedido = DPedidos.obtenerPorId(pedidoId);
            if (pedido == null) return "Error: No existe un pedido con ID " + pedidoId + ".";
            DEnvases env = DEnvases.obtenerPorId(envaseId);
            if (env == null) return "Error: No existe un envase con ID " + envaseId + ".";
            if (env.getStock_disponible() < cantidad) {
                return "Error: Stock insuficiente. Disponible: " + env.getStock_disponible() + ", Solicitado: " + cantidad + ".";
            }
            DPedidoEnvase pe = new DPedidoEnvase();
            pe.setCantidad_prestada(cantidad);
            pe.setCantidad_devuelta(0);
            pe.setFecha_devolucion(null);
            pe.setPedido_origen_id(pedidoId);
            pe.setPedido_devolucion_id(null);
            pe.setEnvase_id(envaseId);
            pe.insertar();
            env.setStock_disponible(env.getStock_disponible() - cantidad);
            env.modificar();
            return "Éxito: Préstamo de " + cantidad + " '" + env.getNombre() + "' registrado para el pedido #" + pedidoId + ". Disponible restante: " + env.getStock_disponible() + ".";
        } catch (NumberFormatException e) {
            return "Error: Los IDs y la cantidad deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU3_05: DEVENV["pedido_origen_id","envase_id","cantidad"]
    public static String registrarDevolucion(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: pedido_origen_id, envase_id, cantidad.";
        try {
            int pedidoOrigenId = Integer.parseInt(p.get(0).trim());
            int envaseId = Integer.parseInt(p.get(1).trim());
            int cantidad = Integer.parseInt(p.get(2).trim());
            if (cantidad <= 0) return "Error: La cantidad debe ser mayor a 0.";
            // Buscar el registro de pedido_envase
            List<DPedidoEnvase> registros = DPedidoEnvase.listarPorPedidoOrigen(pedidoOrigenId);
            DPedidoEnvase registro = null;
            for (DPedidoEnvase pe : registros) {
                if (pe.getEnvase_id() == envaseId) { registro = pe; break; }
            }
            if (registro == null) return "Error: No existe un préstamo de envase ID " + envaseId + " para el pedido #" + pedidoOrigenId + ".";
            int pendiente = registro.getCantidad_prestada() - registro.getCantidad_devuelta();
            if (cantidad > pendiente) {
                return "Error: Se intenta devolver " + cantidad + " pero solo hay " + pendiente + " pendiente(s) de devolución.";
            }
            registro.setCantidad_devuelta(registro.getCantidad_devuelta() + cantidad);
            if (registro.getCantidad_devuelta() >= registro.getCantidad_prestada()) {
                registro.setFecha_devolucion(new Timestamp(System.currentTimeMillis()));
            }
            registro.modificar();
            DEnvases env = DEnvases.obtenerPorId(envaseId);
            if (env != null) {
                env.setStock_disponible(env.getStock_disponible() + cantidad);
                env.modificar();
            }
            int nuevoPendiente = registro.getCantidad_prestada() - registro.getCantidad_devuelta();
            return "Éxito: Devolución de " + cantidad + " '" + (env != null ? env.getNombre() : "envase") + "' registrada. Pendientes: " + nuevoPendiente + ".";
        } catch (NumberFormatException e) {
            return "Error: Los IDs y la cantidad deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU3_06: PENDENV
    public static String verEnvasesPendientes(List<String> p) {
        try {
            List<DPedidoEnvase> lista = DPedidoEnvase.listarPendientes();
            if (lista.isEmpty()) return "No hay envases con devoluciones pendientes.";
            StringBuilder sb = new StringBuilder();
            sb.append("Envase | Cliente | Pedido | Prestados | Devueltos | Pendientes\n");
            sb.append("---\n");
            for (DPedidoEnvase pe : lista) {
                DEnvases env = DEnvases.obtenerPorId(pe.getEnvase_id());
                String nombreEnv = env != null ? env.getNombre() : "ID:" + pe.getEnvase_id();
                DPedidos pedido = DPedidos.obtenerPorId(pe.getPedido_origen_id());
                String nombreCliente = "-";
                if (pedido != null) {
                    DUsuarios usr = DUsuarios.obtenerPorId(pedido.getUsuario_id());
                    if (usr != null) nombreCliente = usr.getNombre() + " " + usr.getApellido();
                }
                int pendiente = pe.getCantidad_prestada() - pe.getCantidad_devuelta();
                sb.append(nombreEnv).append(" | ")
                  .append(nombreCliente).append(" | ")
                  .append("#").append(pe.getPedido_origen_id()).append(" | ")
                  .append(pe.getCantidad_prestada()).append(" | ")
                  .append(pe.getCantidad_devuelta()).append(" | ")
                  .append(pendiente).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar envases pendientes: " + e.getMessage();
        }
    }

    // CU3_07: HISENV["usuario_id"]
    public static String verHistorialEnvasesCliente(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del usuario.";
        try {
            int usuarioId = Integer.parseInt(p.get(0).trim());
            DUsuarios usuario = DUsuarios.obtenerPorId(usuarioId);
            if (usuario == null) return "Error: No existe un usuario con ID " + usuarioId + ".";
            List<DPedidoEnvase> lista = DPedidoEnvase.listarPorUsuario(usuarioId);
            if (lista.isEmpty()) return "El cliente '" + usuario.getNombre() + " " + usuario.getApellido() + "' no tiene historial de envases.";
            StringBuilder sb = new StringBuilder();
            sb.append("Historial de Envases - ").append(usuario.getNombre()).append(" ").append(usuario.getApellido()).append("\n\n");
            sb.append("Envase | Pedido | Prestados | Devueltos | Pendientes | Fecha Devolución\n");
            sb.append("---\n");
            for (DPedidoEnvase pe : lista) {
                DEnvases env = DEnvases.obtenerPorId(pe.getEnvase_id());
                String nombreEnv = env != null ? env.getNombre() : "ID:" + pe.getEnvase_id();
                int pendiente = pe.getCantidad_prestada() - pe.getCantidad_devuelta();
                sb.append(nombreEnv).append(" | ")
                  .append("#").append(pe.getPedido_origen_id()).append(" | ")
                  .append(pe.getCantidad_prestada()).append(" | ")
                  .append(pe.getCantidad_devuelta()).append(" | ")
                  .append(pendiente).append(" | ")
                  .append(pe.getFecha_devolucion() != null ? pe.getFecha_devolucion().toString().substring(0, 10) : "-").append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

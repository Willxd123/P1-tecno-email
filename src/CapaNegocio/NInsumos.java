package CapaNegocio;

import CapaDatos.DInsumos;
import CapaDatos.DMovimientosInsumo;
import CapaDatos.DRecetaDetalle;
import CapaDatos.DRecetas;
import CapaDatos.enums.TipoMovimientoInsumo;
import CapaDatos.enums.UnidadMedida;

import java.util.List;

public class NInsumos {

    // CU2_01: REGINSM["nombre","unidad","stock_inicial","stock_minimo","costo"]
    public static String registrarInsumo(List<String> p) {
        if (p.size() < 5) return "Error: Se requieren 5 parámetros: nombre, unidad_medida, stock_inicial, stock_minimo, costo_unitario.";
        try {
            String nombre = p.get(0).trim();
            String unidadStr = p.get(1).trim().toLowerCase();
            double stockInicial = Double.parseDouble(p.get(2).trim());
            double stockMinimo = Double.parseDouble(p.get(3).trim());
            double costo = Double.parseDouble(p.get(4).trim());
            if (nombre.isEmpty()) return "Error: El nombre del insumo no puede estar vacío.";
            if (stockInicial < 0) return "Error: El stock inicial no puede ser negativo.";
            if (stockMinimo < 0) return "Error: El stock mínimo no puede ser negativo.";
            if (costo < 0) return "Error: El costo no puede ser negativo.";
            UnidadMedida unidad;
            try {
                unidad = UnidadMedida.valueOf(unidadStr);
            } catch (IllegalArgumentException e) {
                return "Error: Unidad de medida inválida. Valores válidos: kg, g, l, ml, unidad.";
            }
            DInsumos ins = new DInsumos();
            ins.setNombre(nombre);
            ins.setUnidad_medida(unidad);
            ins.setStock_actual(stockInicial);
            ins.setStock_minimo(stockMinimo);
            ins.setCosto_unitario(costo);
            if (ins.insertar()) {
                // Registrar movimiento de entrada inicial
                DMovimientosInsumo mov = new DMovimientosInsumo();
                mov.setTipo(TipoMovimientoInsumo.entrada);
                mov.setCantidad(stockInicial);
                mov.setDescripcion("Stock inicial al registrar insumo");
                mov.setInsumo_id(ins.getId());
                mov.setPedido_id(null);
                mov.insertar();
                return "Éxito: Insumo '" + nombre + "' registrado con ID " + ins.getId() + ". Stock inicial: " + stockInicial + " " + unidad.name() + ".";
            }
            return "Error: No se pudo registrar el insumo.";
        } catch (NumberFormatException e) {
            return "Error: Los valores numéricos (stock, costo) son inválidos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_02: EDTINSM["id","nombre","costo","stock_minimo"]
    public static String editarInsumo(List<String> p) {
        if (p.size() < 4) return "Error: Se requieren 4 parámetros: id, nombre, costo_unitario, stock_minimo.";
        try {
            int id = Integer.parseInt(p.get(0).trim());
            String nombre = p.get(1).trim();
            double costo = Double.parseDouble(p.get(2).trim());
            double stockMin = Double.parseDouble(p.get(3).trim());
            if (nombre.isEmpty()) return "Error: El nombre no puede estar vacío.";
            if (costo < 0) return "Error: El costo no puede ser negativo.";
            if (stockMin < 0) return "Error: El stock mínimo no puede ser negativo.";
            DInsumos ins = DInsumos.obtenerPorId(id);
            if (ins == null) return "Error: No existe un insumo con ID " + id + ".";
            ins.setNombre(nombre);
            ins.setCosto_unitario(costo);
            ins.setStock_minimo(stockMin);
            if (ins.modificar()) return "Éxito: Insumo ID " + id + " actualizado correctamente.";
            return "Error: No se pudo actualizar el insumo.";
        } catch (NumberFormatException e) {
            return "Error: ID, costo y stock_minimo deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_03: LISINSM
    public static String listarInsumos(List<String> p) {
        try {
            List<DInsumos> lista = DInsumos.listar();
            if (lista.isEmpty()) return "No hay insumos registrados.";
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre | Unidad | Stock Actual | Stock Mínimo | Costo | Estado\n");
            sb.append("---\n");
            for (DInsumos ins : lista) {
                String estado = ins.getStock_actual() < ins.getStock_minimo() ? "CRÍTICO" : "OK";
                sb.append(ins.getId()).append(" | ")
                  .append(ins.getNombre()).append(" | ")
                  .append(ins.getUnidad_medida().name()).append(" | ")
                  .append(String.format("%.3f", ins.getStock_actual())).append(" | ")
                  .append(String.format("%.3f", ins.getStock_minimo())).append(" | ")
                  .append(String.format("%.4f", ins.getCosto_unitario())).append(" | ")
                  .append(estado).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar insumos: " + e.getMessage();
        }
    }

    // CU2_04: ENTINSM["insumo_id","cantidad","descripcion"]
    public static String entradaStock(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: insumo_id, cantidad, descripcion.";
        try {
            int insumoId = Integer.parseInt(p.get(0).trim());
            double cantidad = Double.parseDouble(p.get(1).trim());
            String desc = p.get(2).trim();
            if (cantidad <= 0) return "Error: La cantidad debe ser mayor a 0.";
            DInsumos ins = DInsumos.obtenerPorId(insumoId);
            if (ins == null) return "Error: No existe un insumo con ID " + insumoId + ".";
            DMovimientosInsumo mov = new DMovimientosInsumo();
            mov.setTipo(TipoMovimientoInsumo.entrada);
            mov.setCantidad(cantidad);
            mov.setDescripcion(desc);
            mov.setInsumo_id(insumoId);
            mov.setPedido_id(null);
            mov.insertar();
            ins.setStock_actual(ins.getStock_actual() + cantidad);
            ins.modificar();
            return "Éxito: Entrada de " + cantidad + " " + ins.getUnidad_medida().name() + " de '" + ins.getNombre() + "'. Nuevo stock: " + String.format("%.3f", ins.getStock_actual()) + ".";
        } catch (NumberFormatException e) {
            return "Error: ID y cantidad deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_05: AJUINSM["insumo_id","cantidad_nueva","descripcion"]
    public static String ajusteStock(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: insumo_id, cantidad_nueva, descripcion.";
        try {
            int insumoId = Integer.parseInt(p.get(0).trim());
            double cantNueva = Double.parseDouble(p.get(1).trim());
            String desc = p.get(2).trim();
            if (cantNueva < 0) return "Error: La cantidad nueva no puede ser negativa.";
            DInsumos ins = DInsumos.obtenerPorId(insumoId);
            if (ins == null) return "Error: No existe un insumo con ID " + insumoId + ".";
            double diferencia = cantNueva - ins.getStock_actual();
            DMovimientosInsumo mov = new DMovimientosInsumo();
            mov.setTipo(TipoMovimientoInsumo.ajuste);
            mov.setCantidad(diferencia);
            mov.setDescripcion(desc);
            mov.setInsumo_id(insumoId);
            mov.setPedido_id(null);
            mov.insertar();
            ins.setStock_actual(cantNueva);
            ins.modificar();
            return "Éxito: Stock de '" + ins.getNombre() + "' ajustado a " + String.format("%.3f", cantNueva) + " " + ins.getUnidad_medida().name() + " (diferencia: " + String.format("%+.3f", diferencia) + ").";
        } catch (NumberFormatException e) {
            return "Error: ID y cantidad deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_06: MERINSM["insumo_id","cantidad","descripcion"]
    public static String mermaStock(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: insumo_id, cantidad, descripcion.";
        try {
            int insumoId = Integer.parseInt(p.get(0).trim());
            double cantidad = Double.parseDouble(p.get(1).trim());
            String desc = p.get(2).trim();
            if (cantidad <= 0) return "Error: La cantidad de merma debe ser mayor a 0.";
            DInsumos ins = DInsumos.obtenerPorId(insumoId);
            if (ins == null) return "Error: No existe un insumo con ID " + insumoId + ".";
            if (ins.getStock_actual() < cantidad) return "Error: Stock insuficiente. Stock actual: " + String.format("%.3f", ins.getStock_actual()) + ".";
            DMovimientosInsumo mov = new DMovimientosInsumo();
            mov.setTipo(TipoMovimientoInsumo.merma);
            mov.setCantidad(-cantidad);
            mov.setDescripcion(desc);
            mov.setInsumo_id(insumoId);
            mov.setPedido_id(null);
            mov.insertar();
            ins.setStock_actual(ins.getStock_actual() - cantidad);
            ins.modificar();
            return "Éxito: Merma de " + cantidad + " " + ins.getUnidad_medida().name() + " de '" + ins.getNombre() + "'. Nuevo stock: " + String.format("%.3f", ins.getStock_actual()) + ".";
        } catch (NumberFormatException e) {
            return "Error: ID y cantidad deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_07: HISINSM["insumo_id"]
    public static String historialMovimientos(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del insumo.";
        try {
            int insumoId = Integer.parseInt(p.get(0).trim());
            DInsumos ins = DInsumos.obtenerPorId(insumoId);
            if (ins == null) return "Error: No existe un insumo con ID " + insumoId + ".";
            List<DMovimientosInsumo> movs = DMovimientosInsumo.listarPorInsumo(insumoId);
            if (movs.isEmpty()) return "No hay movimientos registrados para el insumo '" + ins.getNombre() + "'.";
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Fecha | Tipo | Cantidad | Descripción | Pedido\n");
            sb.append("---\n");
            for (DMovimientosInsumo m : movs) {
                sb.append(m.getId()).append(" | ")
                  .append(m.getFecha() != null ? m.getFecha().toString().substring(0, 16) : "-").append(" | ")
                  .append(m.getTipo().name()).append(" | ")
                  .append(String.format("%+.3f", m.getCantidad())).append(" ").append(ins.getUnidad_medida().name()).append(" | ")
                  .append(m.getDescripcion() != null ? m.getDescripcion() : "-").append(" | ")
                  .append(m.getPedido_id() != null ? "#" + m.getPedido_id() : "-").append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_08: ALEREP
    public static String alertasReposicion(List<String> p) {
        try {
            List<DInsumos> lista = DInsumos.listarStockCritico();
            if (lista.isEmpty()) return "Todos los insumos tienen stock suficiente. No hay alertas de reposición.";
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre | Unidad | Stock Actual | Stock Mínimo | Faltante\n");
            sb.append("---\n");
            for (DInsumos ins : lista) {
                double faltante = ins.getStock_minimo() - ins.getStock_actual();
                sb.append(ins.getId()).append(" | ")
                  .append(ins.getNombre()).append(" | ")
                  .append(ins.getUnidad_medida().name()).append(" | ")
                  .append(String.format("%.3f", ins.getStock_actual())).append(" | ")
                  .append(String.format("%.3f", ins.getStock_minimo())).append(" | ")
                  .append(String.format("%.3f", faltante)).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar alertas: " + e.getMessage();
        }
    }

    // CU2_09: REGREC["producto_id","nombre","descripcion","insumo_id:cantidad",...]
    public static String registrarReceta(List<String> p) {
        if (p.size() < 4) return "Error: Se requieren al menos 4 parámetros: producto_id, nombre, descripcion, insumo_id:cantidad [,...].";
        try {
            int productoId = Integer.parseInt(p.get(0).trim());
            String nombre = p.get(1).trim();
            String desc = p.get(2).trim();
            DRecetas recetaExistente = DRecetas.obtenerPorProducto(productoId);
            if (recetaExistente != null) return "Error: El producto con ID " + productoId + " ya tiene una receta registrada (ID " + recetaExistente.getId() + "). Use EDTREC para editarla.";
            DRecetas receta = new DRecetas();
            receta.setProducto_id(productoId);
            receta.setNombre(nombre);
            receta.setDescripcion(desc);
            if (!receta.insertar()) return "Error: No se pudo registrar la receta. Verifique que el producto existe.";
            int insertados = 0;
            for (int i = 3; i < p.size(); i++) {
                String par = p.get(i).trim();
                if (!par.contains(":")) continue;
                String[] partes = par.split(":");
                int insumoId = Integer.parseInt(partes[0].trim());
                double cant = Double.parseDouble(partes[1].trim());
                DRecetaDetalle rd = new DRecetaDetalle();
                rd.setReceta_id(receta.getId());
                rd.setInsumo_id(insumoId);
                rd.setCantidad(cant);
                rd.insertar();
                insertados++;
            }
            return "Éxito: Receta '" + nombre + "' registrada con ID " + receta.getId() + " para el producto ID " + productoId + ". Se registraron " + insertados + " insumo(s).";
        } catch (NumberFormatException e) {
            return "Error: IDs y cantidades deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_10: EDTREC["receta_id","insumo_id:cantidad",...]
    public static String editarReceta(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren al menos 2 parámetros: receta_id, insumo_id:cantidad [...].";
        try {
            int recetaId = Integer.parseInt(p.get(0).trim());
            // Verificar que exista la receta buscando en la lista
            List<DRecetas> todas = DRecetas.listar();
            DRecetas receta = null;
            for (DRecetas r : todas) {
                if (r.getId() == recetaId) { receta = r; break; }
            }
            if (receta == null) return "Error: No existe una receta con ID " + recetaId + ".";
            // Eliminar detalles existentes
            List<DRecetaDetalle> detallesExistentes = DRecetaDetalle.listarPorReceta(recetaId);
            for (DRecetaDetalle rd : detallesExistentes) {
                rd.eliminar();
            }
            // Insertar nuevos detalles
            int insertados = 0;
            for (int i = 1; i < p.size(); i++) {
                String par = p.get(i).trim();
                if (!par.contains(":")) continue;
                String[] partes = par.split(":");
                int insumoId = Integer.parseInt(partes[0].trim());
                double cant = Double.parseDouble(partes[1].trim());
                DRecetaDetalle rd = new DRecetaDetalle();
                rd.setReceta_id(recetaId);
                rd.setInsumo_id(insumoId);
                rd.setCantidad(cant);
                rd.insertar();
                insertados++;
            }
            return "Éxito: Receta ID " + recetaId + " actualizada. Se registraron " + insertados + " insumo(s).";
        } catch (NumberFormatException e) {
            return "Error: IDs y cantidades deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU2_11: VERREC["producto_id"]
    public static String verReceta(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del producto.";
        try {
            int productoId = Integer.parseInt(p.get(0).trim());
            DRecetas receta = DRecetas.obtenerPorProducto(productoId);
            if (receta == null) return "Error: El producto con ID " + productoId + " no tiene receta registrada.";
            List<DRecetaDetalle> detalles = DRecetaDetalle.listarPorReceta(receta.getId());
            if (detalles.isEmpty()) return "La receta '" + receta.getNombre() + "' no tiene insumos registrados.";
            StringBuilder sb = new StringBuilder();
            sb.append("Receta: ").append(receta.getNombre()).append(" (ID: ").append(receta.getId()).append(")\n");
            sb.append("Descripción: ").append(receta.getDescripcion() != null ? receta.getDescripcion() : "-").append("\n\n");
            sb.append("Insumo | Cantidad | Unidad\n");
            sb.append("---\n");
            for (DRecetaDetalle rd : detalles) {
                DInsumos ins = DInsumos.obtenerPorId(rd.getInsumo_id());
                String nombreIns = ins != null ? ins.getNombre() : "ID:" + rd.getInsumo_id();
                String unidad = ins != null ? ins.getUnidad_medida().name() : "-";
                sb.append(nombreIns).append(" | ")
                  .append(rd.getCantidad()).append(" | ")
                  .append(unidad).append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

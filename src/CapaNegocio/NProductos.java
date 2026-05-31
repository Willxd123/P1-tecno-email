package CapaNegocio;

import CapaDatos.DInsumos;
import CapaDatos.DProductos;
import CapaDatos.DRecetaDetalle;
import CapaDatos.DRecetas;

import java.util.List;

public class NProductos {

    // CU5_01: REGPRO["nombre","descripcion","precio"]
    public static String registrarProducto(List<String> p) {
        if (p.size() < 3) return "Error: Se requieren 3 parámetros: nombre, descripcion, precio_unitario.";
        try {
            String nombre = p.get(0).trim();
            String desc = p.get(1).trim();
            double precio = Double.parseDouble(p.get(2).trim());
            if (nombre.isEmpty()) return "Error: El nombre del producto no puede estar vacío.";
            if (precio <= 0) return "Error: El precio debe ser mayor a 0.";
            DProductos prod = new DProductos();
            prod.setNombre(nombre);
            prod.setDescripcion(desc);
            prod.setPrecio_unitario(precio);
            prod.setDisponible(true);
            if (prod.insertar()) return "Éxito: Producto '" + nombre + "' registrado con ID " + prod.getId() + ".";
            return "Error: No se pudo registrar el producto. Verifique que el nombre no esté duplicado.";
        } catch (NumberFormatException e) {
            return "Error: El precio debe ser un número válido.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU5_02: EDTPRO["id","nombre","descripcion","precio"]
    public static String editarProducto(List<String> p) {
        if (p.size() < 4) return "Error: Se requieren 4 parámetros: id, nombre, descripcion, precio_unitario.";
        try {
            int id = Integer.parseInt(p.get(0).trim());
            String nombre = p.get(1).trim();
            String desc = p.get(2).trim();
            double precio = Double.parseDouble(p.get(3).trim());
            if (nombre.isEmpty()) return "Error: El nombre no puede estar vacío.";
            if (precio <= 0) return "Error: El precio debe ser mayor a 0.";
            DProductos prod = DProductos.obtenerPorId(id);
            if (prod == null) return "Error: No existe un producto con ID " + id + ".";
            prod.setNombre(nombre);
            prod.setDescripcion(desc);
            prod.setPrecio_unitario(precio);
            if (prod.modificar()) return "Éxito: Producto ID " + id + " actualizado correctamente.";
            return "Error: No se pudo actualizar el producto.";
        } catch (NumberFormatException e) {
            return "Error: ID y precio deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU5_03: TOGPRO["id"]
    public static String toggleProducto(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del producto.";
        try {
            int id = Integer.parseInt(p.get(0).trim());
            DProductos prod = DProductos.obtenerPorId(id);
            if (prod == null) return "Error: No existe un producto con ID " + id + ".";
            prod.setDisponible(!prod.isDisponible());
            if (prod.modificar()) {
                String estado = prod.isDisponible() ? "ACTIVADO" : "DESACTIVADO";
                return "Éxito: Producto '" + prod.getNombre() + "' ha sido " + estado + ".";
            }
            return "Error: No se pudo actualizar el estado del producto.";
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU5_04: LISPRO
    public static String listarProductos(List<String> p) {
        try {
            List<DProductos> lista = DProductos.listar();
            if (lista.isEmpty()) return "No hay productos registrados.";
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre | Descripción | Precio (Bs) | Disponible\n");
            sb.append("---\n");
            for (DProductos pr : lista) {
                sb.append(pr.getId()).append(" | ")
                  .append(pr.getNombre()).append(" | ")
                  .append(pr.getDescripcion() != null ? pr.getDescripcion() : "-").append(" | ")
                  .append(String.format("%.2f", pr.getPrecio_unitario())).append(" | ")
                  .append(pr.isDisponible() ? "SÍ" : "NO").append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar productos: " + e.getMessage();
        }
    }

    // CU5_05: COSTPRO["producto_id"]
    public static String verCostoProduccion(List<String> p) {
        if (p.isEmpty()) return "Error: Se requiere el ID del producto.";
        try {
            int productoId = Integer.parseInt(p.get(0).trim());
            DProductos prod = DProductos.obtenerPorId(productoId);
            if (prod == null) return "Error: No existe un producto con ID " + productoId + ".";
            DRecetas receta = DRecetas.obtenerPorProducto(productoId);
            if (receta == null) return "Error: El producto '" + prod.getNombre() + "' no tiene receta registrada.";
            List<DRecetaDetalle> detalles = DRecetaDetalle.listarPorReceta(receta.getId());
            if (detalles.isEmpty()) return "Error: La receta del producto no tiene insumos registrados.";
            StringBuilder sb = new StringBuilder();
            sb.append("Insumo | Cantidad | Unidad | Costo Unitario | Costo Parcial\n");
            sb.append("---\n");
            double costoTotal = 0;
            for (DRecetaDetalle rd : detalles) {
                DInsumos ins = DInsumos.obtenerPorId(rd.getInsumo_id());
                if (ins == null) continue;
                double costoParcial = rd.getCantidad() * ins.getCosto_unitario();
                costoTotal += costoParcial;
                sb.append(ins.getNombre()).append(" | ")
                  .append(rd.getCantidad()).append(" | ")
                  .append(ins.getUnidad_medida().name()).append(" | ")
                  .append(String.format("%.4f", ins.getCosto_unitario())).append(" | ")
                  .append(String.format("%.2f", costoParcial)).append("\n");
            }
            sb.append("\nProducto: ").append(prod.getNombre())
              .append("\nCosto producción: Bs ").append(String.format("%.2f", costoTotal))
              .append("\nPrecio venta: Bs ").append(String.format("%.2f", prod.getPrecio_unitario()))
              .append("\nMargen: Bs ").append(String.format("%.2f", prod.getPrecio_unitario() - costoTotal));
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU5_06: DISPRO["producto_id","cantidad"]
    public static String verificarDisponibilidad(List<String> p) {
        if (p.size() < 2) return "Error: Se requieren 2 parámetros: producto_id, cantidad.";
        try {
            int productoId = Integer.parseInt(p.get(0).trim());
            int cantidad = Integer.parseInt(p.get(1).trim());
            if (cantidad <= 0) return "Error: La cantidad debe ser mayor a 0.";
            DProductos prod = DProductos.obtenerPorId(productoId);
            if (prod == null) return "Error: No existe un producto con ID " + productoId + ".";
            DRecetas receta = DRecetas.obtenerPorProducto(productoId);
            if (receta == null) return "El producto '" + prod.getNombre() + "' no tiene receta. Se puede producir sin restricciones de insumos.";
            List<DRecetaDetalle> detalles = DRecetaDetalle.listarPorReceta(receta.getId());
            StringBuilder sb = new StringBuilder();
            sb.append("Verificación para ").append(cantidad).append(" unidad(es) de '").append(prod.getNombre()).append("':\n\n");
            sb.append("Insumo | Necesario | Disponible | Estado\n");
            sb.append("---\n");
            boolean hayProblema = false;
            for (DRecetaDetalle rd : detalles) {
                DInsumos ins = DInsumos.obtenerPorId(rd.getInsumo_id());
                if (ins == null) continue;
                double necesario = rd.getCantidad() * cantidad;
                boolean ok = ins.getStock_actual() >= necesario;
                if (!ok) hayProblema = true;
                sb.append(ins.getNombre()).append(" | ")
                  .append(String.format("%.3f", necesario)).append(" ").append(ins.getUnidad_medida().name()).append(" | ")
                  .append(String.format("%.3f", ins.getStock_actual())).append(" ").append(ins.getUnidad_medida().name()).append(" | ")
                  .append(ok ? "SÍ" : "NO").append("\n");
            }
            sb.append("\n").append(hayProblema ? "RESULTADO: Stock INSUFICIENTE para producir " + cantidad + " unidad(es)."
                                               : "RESULTADO: Stock SUFICIENTE para producir " + cantidad + " unidad(es).");
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: ID y cantidad deben ser numéricos.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

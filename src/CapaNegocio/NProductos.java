package CapaNegocio;

import CapaDatos.DProductos;
import CapaDatos.DRecetas;
import CapaDatos.DRecetaDetalle;
import CapaDatos.DInsumos;

import java.sql.SQLException;
import java.util.List;

public class NProductos {

    // CU5-01: Registrar Producto
    // Params: ["Nombre", "Descripcion", "PrecioUnitario"]
    public static String registrarProducto(List<String> parametros) {
        if (parametros.size() < 3) {
            return "Error: Faltan parámetros. Uso: CU5-01[\"Nombre\",\"Descripcion\",\"PrecioUnitario\"]";
        }
        try {
            String nombre = parametros.get(0).trim();
            String descripcion = parametros.get(1).trim();
            double precioUnitario = Double.parseDouble(parametros.get(2).trim());

            if (nombre.isEmpty()) {
                return "Error: El nombre del producto no puede estar vacío.";
            }
            if (precioUnitario <= 0) {
                return "Error: El precio unitario debe ser mayor a 0.";
            }

            DProductos prod = new DProductos();
            prod.setNombre(nombre);
            prod.setDescripcion(descripcion);
            prod.setPrecio_unitario(precioUnitario);
            prod.setDisponible(true); // Activo por defecto

            if (prod.insertar()) {
                return "Éxito: Producto '" + nombre + "' registrado correctamente con ID " + prod.getId();
            } else {
                return "Error: No se pudo registrar el producto.";
            }
        } catch (NumberFormatException e) {
            return "Error: El precio unitario debe ser numérico.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU5-02: Editar Producto
    // Params: ["ID", "Nombre", "Descripcion", "PrecioUnitario"]
    public static String editarProducto(List<String> parametros) {
        if (parametros.size() < 4) {
            return "Error: Faltan parámetros. Uso: CU5-02[\"ID\",\"Nombre\",\"Descripcion\",\"PrecioUnitario\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            String nombre = parametros.get(1).trim();
            String descripcion = parametros.get(2).trim();
            double precioUnitario = Double.parseDouble(parametros.get(3).trim());

            DProductos prod = DProductos.obtenerPorId(id);
            if (prod == null) {
                return "Error: No existe el producto con ID " + id;
            }

            if (nombre.isEmpty()) {
                return "Error: El nombre del producto no puede estar vacío.";
            }
            if (precioUnitario <= 0) {
                return "Error: El precio unitario debe ser mayor a 0.";
            }

            prod.setNombre(nombre);
            prod.setDescripcion(descripcion);
            prod.setPrecio_unitario(precioUnitario);

            if (prod.modificar()) {
                return "Éxito: Producto ID " + id + " modificado correctamente.";
            } else {
                return "Error: No se pudo modificar el producto.";
            }
        } catch (NumberFormatException e) {
            return "Error: ID debe ser entero y precio debe ser numérico.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU5-03: Activar o Desactivar Producto
    // Params: ["ID", "Disponible"]
    public static String cambiarDisponibilidad(List<String> parametros) {
        if (parametros.size() < 2) {
            return "Error: Faltan parámetros. Uso: CU5-03[\"ID\",\"Disponible\"] (Disponible: true/false)";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            boolean disponible = Boolean.parseBoolean(parametros.get(1).trim().toLowerCase());

            DProductos prod = DProductos.obtenerPorId(id);
            if (prod == null) {
                return "Error: No existe el producto con ID " + id;
            }

            prod.setDisponible(disponible);

            if (prod.modificar()) {
                return "Éxito: Disponibilidad del producto ID " + id + " establecida en " + disponible + ".";
            } else {
                return "Error: No se pudo actualizar el estado del producto.";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU5-04: Listar Productos
    public static String listarProductos() {
        try {
            List<DProductos> lista = DProductos.listar();
            if (lista.isEmpty()) {
                return "No hay productos registrados en el catálogo.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre | Descripción | Precio Unitario | Disponible\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (DProductos prod : lista) {
                sb.append(prod.getId()).append(" | ")
                  .append(prod.getNombre()).append(" | ")
                  .append(prod.getDescripcion()).append(" | ")
                  .append(prod.getPrecio_unitario()).append(" | ")
                  .append(prod.isDisponible() ? "SÍ" : "NO").append("\n");
            }
            return sb.toString();
        } catch (SQLException e) {
            return "Error al listar productos: " + e.getMessage();
        }
    }

    // CU5-05: Ver Costo de Producción de Producto
    // Params: ["ID"]
    public static String verCostoProduccion(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU5-05[\"ID\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            DProductos prod = DProductos.obtenerPorId(id);
            if (prod == null) {
                return "Error: No existe el producto con ID " + id;
            }

            DRecetas receta = DRecetas.obtenerPorProducto(id);
            if (receta == null) {
                return "Costo de Producción de '" + prod.getNombre() + "':\nEl producto no tiene receta registrada. Costo de Insumos: 0.00 Bs.";
            }

            List<DRecetaDetalle> detalles = DRecetaDetalle.listarPorReceta(receta.getId());
            if (detalles.isEmpty()) {
                return "Costo de Producción de '" + prod.getNombre() + "':\nLa receta está registrada pero no contiene insumos añadidos.";
            }

            double costoTotal = 0.0;
            StringBuilder sb = new StringBuilder();
            sb.append("Desglose de Costo de Producción: ").append(prod.getNombre()).append(" (Receta: ").append(receta.getNombre()).append(")\n");
            sb.append("Insumo | Cantidad Requerida | Costo Unitario | Subtotal\n");
            sb.append("--------------------------------------------------------------------------\n");

            for (DRecetaDetalle det : detalles) {
                DInsumos ins = DInsumos.obtenerPorId(det.getInsumo_id());
                if (ins != null) {
                    double subtotal = det.getCantidad() * ins.getCosto_unitario();
                    costoTotal += subtotal;
                    sb.append(ins.getNombre()).append(" | ")
                      .append(det.getCantidad()).append(" ").append(ins.getUnidad_medida().name()).append(" | ")
                      .append(ins.getCosto_unitario()).append(" Bs | ")
                      .append(subtotal).append(" Bs\n");
                }
            }

            double margen = prod.getPrecio_unitario() - costoTotal;
            double margenPorcentaje = (margen / prod.getPrecio_unitario()) * 100;

            sb.append("--------------------------------------------------------------------------\n");
            sb.append("Costo Total de Insumos: ").append(costoTotal).append(" Bs\n");
            sb.append("Precio de Venta: ").append(prod.getPrecio_unitario()).append(" Bs\n");
            sb.append("Margen de Utilidad: ").append(margen).append(" Bs (").append(String.format("%.2f", margenPorcentaje)).append("%)\n");

            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID del producto debe ser un entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU2-09: Registrar Receta de Producto
    // Params: ["ProductoID", "NombreReceta", "Descripcion"]
    public static String registrarReceta(List<String> parametros) {
        if (parametros.size() < 3) {
            return "Error: Faltan parámetros. Uso: CU2-09[\"ProductoID\",\"NombreReceta\",\"Descripcion\"]";
        }
        try {
            int productoId = Integer.parseInt(parametros.get(0).trim());
            String nombre = parametros.get(1).trim();
            String descripcion = parametros.get(2).trim();

            DProductos prod = DProductos.obtenerPorId(productoId);
            if (prod == null) {
                return "Error: No existe el producto con ID " + productoId;
            }

            if (nombre.isEmpty()) {
                return "Error: El nombre de la receta no puede estar vacío.";
            }

            DRecetas rec = new DRecetas();
            rec.setProducto_id(productoId);
            rec.setNombre(nombre);
            rec.setDescripcion(descripcion);

            if (rec.insertar()) {
                return "Éxito: Receta '" + nombre + "' registrada correctamente para el producto '" + prod.getNombre() + "' con ID " + rec.getId();
            } else {
                return "Error: No se pudo registrar la receta. Recuerda que cada producto solo puede tener una receta única.";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID del producto debe ser entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU2-10: Agregar Insumo a Receta
    // Params: ["RecetaID", "InsumoID", "Cantidad"]
    public static String agregarInsumoAReceta(List<String> parametros) {
        if (parametros.size() < 3) {
            return "Error: Faltan parámetros. Uso: CU2-10[\"RecetaID\",\"InsumoID\",\"Cantidad\"]";
        }
        try {
            int recetaId = Integer.parseInt(parametros.get(0).trim());
            int insumoId = Integer.parseInt(parametros.get(1).trim());
            double cantidad = Double.parseDouble(parametros.get(2).trim());

            DInsumos ins = DInsumos.obtenerPorId(insumoId);
            if (ins == null) {
                return "Error: No existe el insumo con ID " + insumoId;
            }
            if (cantidad <= 0) {
                return "Error: La cantidad de insumo en la receta debe ser mayor a 0.";
            }

            DRecetaDetalle det = new DRecetaDetalle();
            det.setReceta_id(recetaId);
            det.setInsumo_id(insumoId);
            det.setCantidad(cantidad);

            if (det.insertar()) {
                return "Éxito: Insumo '" + ins.getNombre() + "' agregado correctamente a la receta.";
            } else {
                return "Error: No se pudo agregar el insumo a la receta (verifique si ya existe en ella).";
            }
        } catch (NumberFormatException e) {
            return "Error: IDs deben ser enteros y cantidad debe ser numérica.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU2-11: Ver Receta de Producto
    // Params: ["ProductoID"]
    public static String verReceta(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU2-11[\"ProductoID\"]";
        }
        try {
            int productoId = Integer.parseInt(parametros.get(0).trim());
            DProductos prod = DProductos.obtenerPorId(productoId);
            if (prod == null) {
                return "Error: No existe el producto con ID " + productoId;
            }

            DRecetas receta = DRecetas.obtenerPorProducto(productoId);
            if (receta == null) {
                return "Receta de '" + prod.getNombre() + "': No se encuentra ninguna receta registrada.";
            }

            List<DRecetaDetalle> detalles = DRecetaDetalle.listarPorReceta(receta.getId());
            if (detalles.isEmpty()) {
                return "Receta de '" + prod.getNombre() + "' (" + receta.getNombre() + "):\nLa receta está vacía, no tiene insumos registrados.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Detalle de Receta: ").append(receta.getNombre()).append(" (Producto: ").append(prod.getNombre()).append(")\n");
            sb.append("Insumo | Cantidad Requerida | Unidad\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (DRecetaDetalle det : detalles) {
                DInsumos ins = DInsumos.obtenerPorId(det.getInsumo_id());
                String insNombre = ins != null ? ins.getNombre() : "Insumo #" + det.getInsumo_id();
                String unidad = ins != null ? ins.getUnidad_medida().name() : "";
                sb.append(insNombre).append(" | ")
                  .append(det.getCantidad()).append(" | ")
                  .append(unidad).append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID del producto debe ser un entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }
}

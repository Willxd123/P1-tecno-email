package CapaNegocio;

import CapaDatos.DInsumos;
import CapaDatos.DMovimientosInsumo;
import CapaDatos.enums.UnidadMedida;
import CapaDatos.enums.TipoMovimientoInsumo;

import java.sql.SQLException;
import java.util.List;

public class NInsumos {

    // CU2-01: Registrar Insumo
    // Params: ["Nombre", "UnidadMedida", "StockActual", "StockMinimo", "CostoUnitario"]
    public static String registrarInsumo(List<String> parametros) {
        if (parametros.size() < 5) {
            return "Error: Faltan parámetros. Uso: CU2-01[\"Nombre\",\"UnidadMedida\",\"StockActual\",\"StockMinimo\",\"CostoUnitario\"]";
        }
        try {
            String nombre = parametros.get(0).trim();
            String unidadMedidaStr = parametros.get(1).trim().toLowerCase();
            double stockActual = Double.parseDouble(parametros.get(2).trim());
            double stockMinimo = Double.parseDouble(parametros.get(3).trim());
            double costoUnitario = Double.parseDouble(parametros.get(4).trim());

            if (nombre.isEmpty()) {
                return "Error: El nombre del insumo no puede estar vacío.";
            }

            UnidadMedida unidad;
            try {
                unidad = UnidadMedida.valueOf(unidadMedidaStr);
            } catch (IllegalArgumentException e) {
                return "Error: Unidad de medida inválida. Valores permitidos: kg, g, l, ml, unidad";
            }

            if (stockActual < 0 || stockMinimo < 0 || costoUnitario <= 0) {
                return "Error: Los valores numéricos de stock deben ser >= 0 y el costo debe ser > 0.";
            }

            DInsumos insumo = new DInsumos();
            insumo.setNombre(nombre);
            insumo.setUnidad_medida(unidad);
            insumo.setStock_actual(stockActual);
            insumo.setStock_minimo(stockMinimo);
            insumo.setCosto_unitario(costoUnitario);

            if (insumo.insertar()) {
                return "Éxito: Insumo '" + nombre + "' registrado correctamente con ID " + insumo.getId();
            } else {
                return "Error: No se pudo registrar el insumo.";
            }
        } catch (NumberFormatException e) {
            return "Error: Los campos de stock y costo deben ser numéricos.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU2-02: Editar Insumo
    // Params: ["ID", "Nombre", "CostoUnitario", "StockMinimo"]
    public static String editarInsumo(List<String> parametros) {
        if (parametros.size() < 4) {
            return "Error: Faltan parámetros. Uso: CU2-02[\"ID\",\"Nombre\",\"CostoUnitario\",\"StockMinimo\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            String nombre = parametros.get(1).trim();
            double costoUnitario = Double.parseDouble(parametros.get(2).trim());
            double stockMinimo = Double.parseDouble(parametros.get(3).trim());

            DInsumos insumoFetched = DInsumos.obtenerPorId(id);
            if (insumoFetched == null) {
                return "Error: No existe el insumo con ID " + id;
            }

            if (nombre.isEmpty()) {
                return "Error: El nombre del insumo no puede estar vacío.";
            }
            if (costoUnitario <= 0 || stockMinimo < 0) {
                return "Error: El costo debe ser > 0 y el stock mínimo >= 0.";
            }

            insumoFetched.setNombre(nombre);
            insumoFetched.setCosto_unitario(costoUnitario);
            insumoFetched.setStock_minimo(stockMinimo);

            if (insumoFetched.modificar()) {
                return "Éxito: Insumo ID " + id + " modificado correctamente.";
            } else {
                return "Error: No se pudo modificar el insumo.";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser entero y los valores de costo/stock deben ser numéricos.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU2-03: Listar Insumos
    public static String listarInsumos() {
        try {
            List<DInsumos> lista = DInsumos.listar();
            if (lista.isEmpty()) {
                return "No hay insumos registrados en el catálogo.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre | Unidad | Stock Actual | Stock Mínimo | Costo Unitario\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (DInsumos ins : lista) {
                sb.append(ins.getId()).append(" | ")
                  .append(ins.getNombre()).append(" | ")
                  .append(ins.getUnidad_medida().name()).append(" | ")
                  .append(ins.getStock_actual()).append(" | ")
                  .append(ins.getStock_minimo()).append(" | ")
                  .append(ins.getCosto_unitario()).append("\n");
            }
            return sb.toString();
        } catch (SQLException e) {
            return "Error al listar insumos: " + e.getMessage();
        }
    }

    // CU2-04: Registrar Entrada de Stock
    // Params: ["ID", "Cantidad", "Descripcion"]
    public static String registrarEntrada(List<String> parametros) {
        return registrarMovimiento(parametros, TipoMovimientoInsumo.entrada);
    }

    // CU2-05: Registrar Ajuste de Stock
    // Params: ["ID", "Cantidad", "Descripcion"]
    public static String registrarAjuste(List<String> parametros) {
        return registrarMovimiento(parametros, TipoMovimientoInsumo.ajuste);
    }

    // CU2-06: Registrar Merma
    // Params: ["ID", "Cantidad", "Descripcion"]
    public static String registrarMerma(List<String> parametros) {
        return registrarMovimiento(parametros, TipoMovimientoInsumo.merma);
    }

    private static String registrarMovimiento(List<String> parametros, TipoMovimientoInsumo tipo) {
        if (parametros.size() < 3) {
            return "Error: Faltan parámetros. Uso: [" + tipo.name().toUpperCase() + "][\"ID\",\"Cantidad\",\"Descripcion\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            double cantidad = Double.parseDouble(parametros.get(1).trim());
            String descripcion = parametros.get(2).trim();

            DInsumos insumo = DInsumos.obtenerPorId(id);
            if (insumo == null) {
                return "Error: No existe el insumo con ID " + id;
            }

            double nuevaCantidad = cantidad;
            if (tipo == TipoMovimientoInsumo.merma) {
                // Las mermas restan stock, por lo que cantidad debe ser negativa
                if (cantidad > 0) {
                    nuevaCantidad = -cantidad;
                }
            }

            double nuevoStock = insumo.getStock_actual() + nuevaCantidad;
            if (nuevoStock < 0) {
                return "Error: La operación resultaría en stock negativo (" + nuevoStock + "). Stock actual: " + insumo.getStock_actual();
            }

            insumo.setStock_actual(nuevoStock);

            if (insumo.modificar()) {
                // Registrar movimiento
                DMovimientosInsumo mov = new DMovimientosInsumo();
                mov.setInsumo_id(id);
                mov.setTipo(tipo);
                mov.setCantidad(nuevaCantidad);
                mov.setDescripcion(descripcion);
                mov.setPedido_id(null);

                if (mov.insertar()) {
                    return "Éxito: Movimiento de " + tipo.name() + " registrado. Nuevo stock de '" + insumo.getNombre() + "': " + nuevoStock;
                } else {
                    return "Error: Se actualizó el stock, pero no se pudo registrar el movimiento.";
                }
            } else {
                return "Error: No se pudo actualizar el stock del insumo.";
            }

        } catch (NumberFormatException e) {
            return "Error: El ID debe ser entero y la cantidad debe ser numérica.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU2-07: Ver Historial de Movimientos
    // Params: ["InsumoID"]
    public static String verHistorialMovimientos(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta parámetro. Uso: CU2-07[\"InsumoID\"]";
        }
        try {
            int insumoId = Integer.parseInt(parametros.get(0).trim());
            DInsumos insumo = DInsumos.obtenerPorId(insumoId);
            if (insumo == null) {
                return "Error: No existe el insumo con ID " + insumoId;
            }

            List<DMovimientosInsumo> lista = DMovimientosInsumo.listarPorInsumo(insumoId);
            if (lista.isEmpty()) {
                return "No hay movimientos registrados para el insumo '" + insumo.getNombre() + "'.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Historial de Movimientos de Insumo: ").append(insumo.getNombre()).append("\n");
            sb.append("Fecha | Tipo | Cantidad | Descripción | Pedido ID\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (DMovimientosInsumo mov : lista) {
                String pedStr = mov.getPedido_id() != null ? String.valueOf(mov.getPedido_id()) : "N/A";
                sb.append(mov.getFecha()).append(" | ")
                  .append(mov.getTipo().name()).append(" | ")
                  .append(mov.getCantidad()).append(" | ")
                  .append(mov.getDescripcion()).append(" | ")
                  .append(pedStr).append("\n");
            }
            return sb.toString();
        } catch (NumberFormatException e) {
            return "Error: El ID del insumo debe ser entero.";
        } catch (SQLException e) {
            return "Error en BD: " + e.getMessage();
        }
    }

    // CU2-08: Ver Alertas de Reposición
    public static String verAlertasReposicion() {
        try {
            List<DInsumos> lista = DInsumos.listar();
            StringBuilder sb = new StringBuilder();
            boolean found = false;
            sb.append("Alertas de Reposición de Insumos (Stock por debajo del mínimo):\n");
            sb.append("ID | Nombre | Stock Actual | Stock Mínimo | Faltante\n");
            sb.append("--------------------------------------------------------------------------\n");
            for (DInsumos ins : lista) {
                if (ins.getStock_actual() < ins.getStock_minimo()) {
                    double faltante = ins.getStock_minimo() - ins.getStock_actual();
                    sb.append(ins.getId()).append(" | ")
                      .append(ins.getNombre()).append(" | ")
                      .append(ins.getStock_actual()).append(" | ")
                      .append(ins.getStock_minimo()).append(" | ")
                      .append(faltante).append("\n");
                    found = true;
                }
            }
            return found ? sb.toString() : "Todo bien. Ningún insumo requiere reposición actualmente.";
        } catch (SQLException e) {
            return "Error al verificar alertas: " + e.getMessage();
        }
    }
}

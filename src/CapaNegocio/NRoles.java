package CapaNegocio;

import CapaDatos.DRoles;
import java.util.List;

public class NRoles {

    /**
     * Registra un nuevo rol.
     * Parámetros: [nombre]
     */
    public static String registrarRol(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta el nombre del rol. Uso: REGROL[\"NombreRol\"]";
        }
        try {
            String nombre = parametros.get(0).trim();
            if (nombre.isEmpty()) {
                return "Error: El nombre del rol no puede estar vacío.";
            }

            // Validar si ya existe
            DRoleExists(nombre);
            if (DRoles.obtenerPorNombre(nombre) != null) {
                return "Error: El rol '" + nombre + "' ya existe en el sistema.";
            }

            DRoles rol = new DRoles();
            rol.setNombre(nombre);

            if (rol.insertar()) {
                return "Éxito: Rol '" + nombre + "' registrado con ID: " + rol.getId();
            } else {
                return "Error: No se pudo registrar el rol.";
            }
        } catch (Exception e) {
            return "Error al registrar el rol: " + e.getMessage();
        }
    }

    /**
     * Edita un rol existente.
     * Parámetros: [id] [nombre]
     */
    public static String editarRol(List<String> parametros) {
        if (parametros.size() < 2) {
            return "Error: Faltan parámetros. Uso: EDTROL[\"ID\",\"NuevoNombre\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            String nuevoNombre = parametros.get(1).trim();

            if (nuevoNombre.isEmpty()) {
                return "Error: El nombre del rol no puede estar vacío.";
            }

            DRoles rol = DRoles.obtenerPorId(id);
            if (rol == null) {
                return "Error: Rol con ID " + id + " no encontrado.";
            }

            // Validar que el nuevo nombre no esté en uso por otro ID
            DRoles existente = DRoles.obtenerPorNombre(nuevoNombre);
            if (existente != null && existente.getId() != id) {
                return "Error: Ya existe otro rol con el nombre '" + nuevoNombre + "'.";
            }

            rol.setNombre(nuevoNombre);
            if (rol.modificar()) {
                return "Éxito: Rol modificado correctamente. ID: " + id + " | Nuevo Nombre: " + nuevoNombre;
            } else {
                return "Error: No se pudo modificar el rol.";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID del rol debe ser un número entero.";
        } catch (Exception e) {
            return "Error al modificar el rol: " + e.getMessage();
        }
    }

    /**
     * Elimina un rol existente.
     * Parámetros: [id]
     */
    public static String eliminarRol(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta el ID del rol. Uso: DELROL[\"ID\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());

            DRoles rol = DRoles.obtenerPorId(id);
            if (rol == null) {
                return "Error: Rol con ID " + id + " no encontrado.";
            }

            if (rol.eliminar()) {
                return "Éxito: Rol con ID " + id + " (" + rol.getNombre() + ") eliminado correctamente.";
            } else {
                return "Error: No se pudo eliminar el rol (puede estar asociado a usuarios existentes).";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID del rol debe ser un número entero.";
        } catch (Exception e) {
            return "Error al eliminar el rol: " + e.getMessage();
        }
    }

    /**
     * Lista todos los roles.
     * Parámetros: ninguno
     */
    public static String listarRoles(List<String> parametros) {
        try {
            List<DRoles> lista = DRoles.listar();
            if (lista.isEmpty()) {
                return "No hay roles registrados en el sistema.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== LISTA DE ROLES ===\n");
            sb.append("ID  | Nombre del Rol\n");
            sb.append("--------------------\n");
            for (DRoles r : lista) {
                sb.append(r.getId()).append("   | ").append(r.getNombre()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar roles: " + e.getMessage();
        }
    }

    /**
     * Obtiene los detalles de un rol específico.
     * Parámetros: [id]
     */
    public static String verRol(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Falta el ID del rol. Uso: VERROL[\"ID\"]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());

            DRoles rol = DRoles.obtenerPorId(id);
            if (rol == null) {
                return "Error: Rol con ID " + id + " no encontrado.";
            }

            return "=== DETALLE DEL ROL ===\n" +
                   "ID:     " + rol.getId() + "\n" +
                   "Nombre: " + rol.getNombre();
        } catch (NumberFormatException e) {
            return "Error: El ID del rol debe ser un número entero.";
        } catch (Exception e) {
            return "Error al obtener rol: " + e.getMessage();
        }
    }

    // Helper para comprobación rápida de inicialización
    private static void DRoleExists(String name) {}
}

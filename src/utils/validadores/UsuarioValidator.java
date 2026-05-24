package utils.validadores;

import CapaDatos.enums.RolNombre;

/**
 * Validador de reglas de negocio para la gestión de Usuarios.
 */
public class UsuarioValidator {

    public static void validarNombre(String nombre) throws IllegalArgumentException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (nombre.length() > 80) {
            throw new IllegalArgumentException("El nombre es demasiado largo (máximo 80 caracteres).");
        }
    }

    public static void validarApellido(String apellido) throws IllegalArgumentException {
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío.");
        }
        if (apellido.length() > 80) {
            throw new IllegalArgumentException("El apellido es demasiado largo (máximo 80 caracteres).");
        }
    }

    public static void validarTelefono(String telefono) throws IllegalArgumentException {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío.");
        }
        if (!telefono.matches("\\d{7,15}")) {
            throw new IllegalArgumentException("El teléfono debe contener entre 7 y 15 dígitos numéricos.");
        }
    }

    public static void validarPassword(String password) throws IllegalArgumentException {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
    }

    public static RolNombre validarRol(String rolStr) throws IllegalArgumentException {
        if (rolStr == null || rolStr.trim().isEmpty()) {
            throw new IllegalArgumentException("El rol no puede estar vacío.");
        }
        try {
            // Convierte ignorando mayúsculas/minúsculas para mayor flexibilidad en el correo
            for (RolNombre r : RolNombre.values()) {
                if (r.name().equalsIgnoreCase(rolStr.trim())) {
                    return r;
                }
            }
            throw new IllegalArgumentException("Rol inválido. Roles permitidos: Propietario, Secretaria, Cliente.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Rol inválido. Roles permitidos: Propietario, Secretaria, Cliente.");
        }
    }
}

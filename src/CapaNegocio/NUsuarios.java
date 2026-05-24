package CapaNegocio;

import CapaDatos.DRoles;
import CapaDatos.DUsuarios;
import CapaDatos.enums.RolNombre;
import utils.validadores.UsuarioValidator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class NUsuarios {

    // CU1-01: Registrar usuario
    // Formato de parámetros: [nombre] [apellido] [telefono] [email] [password]
    // [rol]
    public static String registrarUsuario(List<String> parametros) {
        if (parametros.size() < 6) {
            return "Error: Faltan parámetros. Uso: CU1-01 [nombre] [apellido] [telefono] [email] [password] [rol]";
        }
        try {
            String nombre = parametros.get(0);
            String apellido = parametros.get(1);
            String telefono = parametros.get(2);
            String email = parametros.get(3);
            String password = parametros.get(4);
            String rolStr = parametros.get(5);

            // Validaciones
            UsuarioValidator.validarNombre(nombre);
            UsuarioValidator.validarApellido(apellido);
            UsuarioValidator.validarTelefono(telefono);
            UsuarioValidator.validarPassword(password);
            RolNombre rolNombre = UsuarioValidator.validarRol(rolStr);

            // Obtener el ID del rol
            DRoles dRol = DRoles.obtenerPorNombre(rolNombre.name());
            if (dRol == null) {
                return "Error: El rol '" + rolNombre.name() + "' no está registrado en la base de datos.";
            }

            // Hashear contraseña
            String hash = hashPassword(password);

            // Guardar
            DUsuarios usuario = new DUsuarios();
            usuario.setNombre(nombre.trim());
            usuario.setApellido(apellido.trim());
            usuario.setTelefono(telefono.trim());
            usuario.setEmail(email.trim());
            usuario.setpassword(hash);
            usuario.setRol_id(dRol.getId());
            usuario.setActivo(true); // Activo por defecto

            if (usuario.insertar()) {
                return "Éxito: Usuario " + nombre + " " + apellido + " registrado correctamente con ID "
                        + usuario.getId();
            } else {
                return "Error: No se pudo registrar el usuario. Verifique si el teléfono ya existe.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU1-02: Editar usuario
    // Formato: CU1-02 [id] [nombre] [apellido] [telefono] [email]
    public static String editarUsuario(List<String> parametros) {
        if (parametros.size() < 5) {
            return "Error: Faltan parámetros. Uso: CU1-02 [id] [nombre] [apellido] [telefono] [email]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            String nombre = parametros.get(1);
            String apellido = parametros.get(2);
            String telefono = parametros.get(3);
            String email = parametros.get(4);

            UsuarioValidator.validarNombre(nombre);
            UsuarioValidator.validarApellido(apellido);
            UsuarioValidator.validarTelefono(telefono);

            DUsuarios usuario = DUsuarios.obtenerPorId(id);
            if (usuario == null) {
                return "Error: No existe un usuario con ID " + id;
            }

            usuario.setNombre(nombre.trim());
            usuario.setApellido(apellido.trim());
            usuario.setTelefono(telefono.trim());
            usuario.setEmail(email.trim());

            if (usuario.modificar()) {
                return "Éxito: Datos del usuario actualizados correctamente.";
            } else {
                return "Error: No se pudo actualizar el usuario.";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU1-03: Cambiar contraseña
    // Formato: CU1-03 [id] [nueva_contraseña]
    public static String cambiarPassword(List<String> parametros) {
        if (parametros.size() < 2) {
            return "Error: Faltan parámetros. Uso: CU1-03 [id] [nueva_contraseña]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            String nuevaPassword = parametros.get(1);

            UsuarioValidator.validarPassword(nuevaPassword);

            DUsuarios usuario = DUsuarios.obtenerPorId(id);
            if (usuario == null) {
                return "Error: No existe un usuario con ID " + id;
            }

            usuario.setpassword(hashPassword(nuevaPassword));

            if (usuario.modificar()) {
                return "Éxito: Contraseña actualizada correctamente.";
            } else {
                return "Error: No se pudo cambiar la contraseña.";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU1-04: Desactivar usuario
    // Formato: CU1-04 [id]
    public static String desactivarUsuario(List<String> parametros) {
        if (parametros.size() < 1) {
            return "Error: Faltan parámetros. Uso: CU1-04 [id]";
        }
        try {
            int id = Integer.parseInt(parametros.get(0).trim());

            DUsuarios usuario = DUsuarios.obtenerPorId(id);
            if (usuario == null) {
                return "Error: No existe un usuario con ID " + id;
            }

            usuario.setActivo(false);

            if (usuario.modificar()) {
                return "Éxito: Usuario desactivado correctamente.";
            } else {
                return "Error: No se pudo desactivar el usuario.";
            }
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // CU1-05: Listar usuarios
    // Formato: CU1-05
    public static String listarUsuarios(List<String> parametros) {
        try {
            List<DUsuarios> lista = DUsuarios.listar();
            if (lista.isEmpty()) {
                return "No hay usuarios registrados.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Nombre Completo | Teléfono | Email | Rol | Activo\n");
            sb.append("----------------------------------------------------------\n");
            for (DUsuarios u : lista) {
                DRoles rol = DRoles.obtenerPorId(u.getRol_id());
                String nombreRol = rol != null ? rol.getNombre() : "Desconocido";
                sb.append(u.getId()).append(" | ")
                        .append(u.getNombre()).append(" ").append(u.getApellido()).append(" | ")
                        .append(u.getTelefono()).append(" | ")
                        .append(u.getEmail()).append(" | ")
                        .append(nombreRol).append(" | ")
                        .append(u.isActivo() ? "SÍ" : "NO").append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error al listar usuarios: " + e.getMessage();
        }
    }

    // CU1-06: Buscar usuario
    // Formato: CU1-06 [texto_a_buscar]
    public static String buscarUsuario(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Faltan parámetros. Uso: CU1-06 [texto]";
        }
        try {
            String texto = parametros.get(0).toLowerCase();
            List<DUsuarios> lista = DUsuarios.listar();
            StringBuilder sb = new StringBuilder();
            sb.append("Resultados de búsqueda:\n");
            boolean found = false;
            for (DUsuarios u : lista) {
                if (String.valueOf(u.getId()).equals(texto) ||
                        u.getNombre().toLowerCase().contains(texto) ||
                        u.getApellido().toLowerCase().contains(texto) ||
                        u.getTelefono().contains(texto)) {

                    DRoles rol = DRoles.obtenerPorId(u.getRol_id());
                    String nombreRol = rol != null ? rol.getNombre() : "Desconocido";
                    sb.append("ID: ").append(u.getId())
                            .append(" - ").append(u.getNombre()).append(" ").append(u.getApellido())
                            .append(" (").append(u.getTelefono()).append(") | Email: ").append(u.getEmail())
                            .append(" [").append(nombreRol).append("]\n");
                    found = true;
                }
            }
            return found ? sb.toString() : "No se encontraron usuarios que coincidan con '" + texto + "'.";
        } catch (Exception e) {
            return "Error al buscar usuario: " + e.getMessage();
        }
    }

    public static String verPerfil(List<String> parametros) {
        if (parametros.isEmpty()) {
            return "Error: Faltan parámetros. Uso: CU1-07 [id]";
        }
        String resultado = "";
        try {
            int id = Integer.parseInt(parametros.get(0).trim());
            DUsuarios usuario = DUsuarios.obtenerPorId(id);
            if (usuario == null) {
                return "Error: No existe un perfil con ID " + id;
            }
            DRoles rol = DRoles.obtenerPorId(usuario.getRol_id());
            String nombreRol = rol != null ? rol.getNombre() : "Desconocido";

            resultado = "--- PERFIL DE USUARIO ---\n" +
                    "ID: " + usuario.getId() + "\n" +
                    "Nombre: " + usuario.getNombre() + " " + usuario.getApellido() + "\n" +
                    "Teléfono: " + usuario.getTelefono() + "\n" +
                    "Email: " + usuario.getEmail() + "\n" +
                    "Rol: " + nombreRol + "\n" +
                    "Activo: " + (usuario.isActivo() ? "Sí" : "No");
        } catch (NumberFormatException e) {
            return "Error: El ID debe ser numérico.";
        } catch (Exception e) {
            return "Error al obtener perfil: " + e.getMessage();
        }
        return resultado;
    }

    // Utilidad interna: Hasheo de contraseña con SHA-256
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error encriptando contraseña", e);
        }
    }
}

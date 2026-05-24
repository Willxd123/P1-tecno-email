package CapaDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import configuracion.Configuracion;

/**
 * Helper de Conexión a la Base de Datos PostgreSQL.
 * Utiliza de manera centralizada los valores configurados en la clase Configuracion (.env).
 */
public class Conexion {

    private static Connection connection = null;

    /**
     * Obtiene una conexión activa a la base de datos PostgreSQL.
     * Si la conexión no existe o está cerrada, crea una nueva.
     */
    public static Connection getConexion() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Registrar el driver de PostgreSQL
                Class.forName("org.postgresql.Driver");

                // Construir la URL de conexión
                String host = Configuracion.getDbHost();
                int port = Configuracion.getDbPort();
                String dbName = Configuracion.getDbName();
                String user = Configuracion.getDbUser();
                String password = Configuracion.getDbPassword();

                String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;

                connection = DriverManager.getConnection(url, user, password);
            } catch (ClassNotFoundException e) {
                System.err.println("[Conexion] ERROR: No se encontró el driver JDBC de PostgreSQL.");
                throw new SQLException(e);
            }
        }
        return connection;
    }

    /**
     * Cierra la conexión activa a la base de datos si existe.
     */
    public static void cerrarConexion() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("[Conexion] ERROR al cerrar la conexión: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}

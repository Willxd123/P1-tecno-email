package CapaDatos;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
    private static Connection proxyConnection = null;

    /**
     * Obtiene una conexión activa a la base de datos PostgreSQL.
     * Si la conexión no existe o está cerrada, crea una nueva.
     */
    public static Connection getConexion() throws SQLException {
        boolean isDead = true;
        if (connection != null) {
            try {
                isDead = connection.isClosed() || !connection.isValid(2);
            } catch (SQLException e) {
                isDead = true;
            }
        }

        if (connection == null || isDead) {
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

                // Crear un proxy para ignorar el método close()
                proxyConnection = (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[] { Connection.class },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("close")) {
                                // Ignorar el cierre de la conexión compartida para evitar
                                // que los bloques try-with-resources la cierren físicamente.
                                return null;
                            }
                            try {
                                return method.invoke(connection, args);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                        }
                    }
                );
            } catch (ClassNotFoundException e) {
                System.err.println("[Conexion] ERROR: No se encontró el driver JDBC de PostgreSQL.");
                throw new SQLException(e);
            }
        }
        return proxyConnection;
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
                proxyConnection = null;
            }
        }
    }
}

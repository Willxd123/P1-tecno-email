package CapaDatos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Inicializador automático de la Base de Datos.
 * Carga y ejecuta el script SQL 'schema.sql' al iniciar la aplicación.
 * Diseñado con compatibilidad absoluta para Java 8+.
 */
public class DatabaseInitializer {

    /**
     * Busca, lee y ejecuta el script de base de datos schema.sql y seeder.sql.
     */
    public static void initializeDatabase() {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            // Desactivar temporalmente autocommit para hacerlo en una sola transaccion por script
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                executeScript(conn, "schema.sql");
                executeScript(conn, "seeder.sql");
                conn.commit();
                System.out.println("[DatabaseInitializer] ¡Base de datos y datos iniciales (seeder) verificados con exito!");
            } catch (Exception e) {
                conn.rollback();
                System.err.println("[DatabaseInitializer] ERROR durante la inicializacion: " + e.getMessage());
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] ERROR al conectar a la base de datos para la inicializacion: " + e.getMessage());
        }
    }

    private static void executeScript(Connection conn, String fileName) throws Exception {
        String[] candidatePaths = {
            "src/configuracion/" + fileName,
            "TecnoEmailZUZU/src/configuracion/" + fileName,
            "../src/configuracion/" + fileName,
            fileName,
            "TecnoEmailZUZU/" + fileName
        };

        File sqlFile = null;
        for (String path : candidatePaths) {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                sqlFile = f;
                break;
            }
        }

        if (sqlFile == null) {
            System.err.println("[DatabaseInitializer] ADVERTENCIA: No se pudo encontrar el archivo '" + fileName + "' en ninguna ruta conocida.");
            return;
        }

        System.out.println("[DatabaseInitializer] Ejecutando script: " + sqlFile.getAbsolutePath());

        try (Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
             
            String line;
            StringBuilder sqlStatement = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                // Ignorar comentarios y líneas vacías
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--") || trimmedLine.startsWith("#")) {
                    continue;
                }

                sqlStatement.append(line).append("\n");

                // Si termina con punto y coma, es el final de una sentencia SQL
                if (trimmedLine.endsWith(";")) {
                    String query = sqlStatement.toString().trim();
                    if (!query.isEmpty()) {
                        stmt.execute(query);
                    }
                    // Reiniciar acumulador
                    sqlStatement.setLength(0);
                }
            }

            // Ejecutar cualquier sentencia remanente que no termine en punto y coma (por si acaso)
            String remainingQuery = sqlStatement.toString().trim();
            if (!remainingQuery.isEmpty()) {
                stmt.execute(remainingQuery);
            }
        }
    }
}

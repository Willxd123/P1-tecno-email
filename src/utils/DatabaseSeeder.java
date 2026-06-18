package utils;

import CapaDatos.Conexion;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("Iniciando la reconstrucción y siembra (Seed) de la Base de Datos...");
        reconstruirYSembrar();
    }

    public static void reconstruirYSembrar() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            System.out.println("1. Limpiando todas las tablas (TRUNCATE CASCADE)...");
            String truncateSql = "TRUNCATE TABLE " +
                "cuotas, pagos, pedido_envase, detalle_pedido, movimientos_insumo, pedidos, cartillas, " +
                "receta_detalle, recetas, productos, categoria_producto, usuarios, roles, insumos, envases " +
                "RESTART IDENTITY CASCADE;";
            stmt.executeUpdate(truncateSql);

            System.out.println("2. Sembrando Roles...");
            stmt.executeUpdate("INSERT INTO roles (id, nombre) VALUES (1, 'Propietario'), (2, 'Secretaria'), (3, 'Cliente');");

            System.out.println("3. Sembrando Categorías de Productos...");
            stmt.executeUpdate("INSERT INTO categoria_producto (id, nombre) VALUES (1, 'Tradicional'), (2, 'Delux');");

            System.out.println("4. Sembrando Usuarios Iniciales...");
            String seedUsuarios = "INSERT INTO usuarios (id, nombre, apellido, telefono, email, password, rol_id, activo) VALUES " +
                "(1, 'Juan', 'Pérez', '77012345', 'juan.perez@gmail.com', 'juan123', 1, true)," +
                "(2, 'Maria', 'Gomez', '77012346', 'maria.admin@chifones.com', 'admin123', 2, true)," +
                "(3, 'Grace', 'Pariona', '77012349', 'tiffanypado@gmail.com', 'grace123', 3, true);";
            stmt.executeUpdate(seedUsuarios);

            System.out.println("5. Sembrando Catálogo de Productos...");
            String seedProductos = "INSERT INTO productos (id, nombre, descripcion, precio_unitario, disponible, categoria_producto_id) VALUES " +
                "(1, 'Chifon de Chocochip', 'Esponjoso chifon de chip, bañado en chcolate y pepitas de chocolate', 90.00, true, 2)," +
                "(2, 'Chifon de Limon', 'Esponjo chifon de limon, con glaseado de limon', 80, true, 1)," +
                "(3, 'Chifon de Naranja', 'Esponjoso chifón de naranja con zumo natural de naranja', 75.00, true, 1)," +
                "(4, 'Chifón de Chocomani', 'Chifón de chocolate con trozos de maní', 90.00, true, 2);";
            stmt.executeUpdate(seedProductos);

            System.out.println("6. Sembrando Insumos de Inventario...");
            String seedInsumos = "INSERT INTO insumos (id, nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario) VALUES " +
                "(1, 'Harina', 'g', 50000.000, 100.000, 0.02)," +
                "(2, 'Azúcar', 'g', 50000.000, 100.000, 0.01)," +
                "(3, 'Mani', 'g', 10000.000, 500.000, 0.03)," +
                "(4, 'Huevos', 'unidad', 3000.000, 100.000, 1.00)," +
                "(5, 'Naranja zumo', 'ml', 5000.000, 30.000, 1.50)," +
                "(6, 'Limon zumo', 'ml', 50000.000, 200.000, 0.02)," +
                "(7, 'Pepitas Chocolate', 'g', 5000.000, 200.000, 0.02)," +
                "(8, 'Chocolate derretido', 'g', 10000.000, 500.000, 0.01);";
            stmt.executeUpdate(seedInsumos);

            System.out.println("7. Sembrando Recetas de Fabricación...");
            String seedRecetas = "INSERT INTO recetas (id, nombre, descripcion, producto_id) VALUES " +
                "(1, 'Receta de Chocochip', 'Fórmula estándar de chocolate', 1)," +
                "(2, 'Receta de Limon', 'Fórmula clásica de limon', 2)," +
                "(3, 'Receta de Naranja', 'Fórmula clásica de naranja', 3)," +
                "(4, 'Receta de Chifón Chocomani', 'Fórmula de chocolate con maní', 4);";
            stmt.executeUpdate(seedRecetas);

            System.out.println("8. Sembrando Detalle de Ingredientes por Receta...");
            String seedRecetaDetalle = "INSERT INTO receta_detalle (id, receta_id, insumo_id, cantidad) VALUES " +
                "(1, 1, 1, 100.000)," +
                "(2, 1, 2, 50.000)," +
                "(3, 1, 7, 10.000)," +
                "(4, 2, 1, 100.000)," +
                "(5, 2, 2, 50.000)," +
                "(6, 2, 6, 10.000)," +
                "(7, 3, 1, 100.000)," +
                "(8, 3, 2, 50.000)," +
                "(9, 3, 5, 10.000)," +
                "(10, 4, 1, 100.000)," +
                "(11, 4, 2, 50.000);";
            stmt.executeUpdate(seedRecetaDetalle);

            System.out.println("9. Sembrando Envases Retornables...");
            String seedEnvases = "INSERT INTO envases (id, nombre, descripcion, stock_total, stock_disponible) VALUES " +
                "(1, 'Envase Plastico', 'Envase plastico retornable', 100, 100)," +
                "(2, 'Envase de Bolsa', 'Envase bolsa plastofor', 100, 100);";
            stmt.executeUpdate(seedEnvases);

            System.out.println("10. Sembrando Cartillas de Fidelización Iniciales...");
            String seedCartillas = "INSERT INTO cartillas (usuario_id, estado, fecha_inicio) VALUES " +
                //"(3, 'activa', CURRENT_TIMESTAMP)," +
                //"(4, 'activa', CURRENT_TIMESTAMP)," +
                "(3, 'activa', CURRENT_TIMESTAMP);";
            stmt.executeUpdate(seedCartillas);

            System.out.println("11. Alineando secuencias autoincrementales en PostgreSQL...");
            String[] tables = {"roles", "categoria_producto", "usuarios", "productos", "insumos", "recetas", "receta_detalle", "envases", "cartillas"};
            for (String table : tables) {
                String seqSql = "SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), coalesce(max(id), 1)) FROM " + table + ";";
                stmt.execute(seqSql);
            }

            conn.commit();
            System.out.println("¡Base de datos reconstruida con éxito");

        } catch (SQLException e) {
            System.err.println("Error durante la reconstrucción de base de datos desde Java: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    /* Ignorar */
                }
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                /* Ignorar */
            }
        }
    }
}

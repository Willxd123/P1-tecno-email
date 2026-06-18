import CapaDatos.Conexion;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class TestDb {
    public static void main(String[] args) {
        System.out.println("Testing DB Connection and Listing Tables...");
        try {
            Connection conn = Conexion.getConexion();
            System.out.println("Connection successful!");
            DatabaseMetaData dbmd = conn.getMetaData();
            
            // List tables
            String[] types = {"TABLE"};
            ResultSet rs = dbmd.getTables(null, null, "%", types);
            System.out.println("Tables in database:");
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                System.out.println(" - " + tableName);
            }
            
            // Let's check if 'persona' table exists
            System.out.println("\nChecking schema of table 'persona' or similar...");
            rs = dbmd.getTables(null, null, "persona", null);
            if (rs.next()) {
                System.out.println("Table 'persona' exists! Columns:");
                ResultSet cols = dbmd.getColumns(null, null, "persona", null);
                while (cols.next()) {
                    System.out.println("   * " + cols.getString("COLUMN_NAME") + " (" + cols.getString("TYPE_NAME") + ")");
                }
            } else {
                System.out.println("Table 'persona' does not exist.");
            }

            // Also check 'usuario' or similar tables
            rs = dbmd.getTables(null, null, "usuario%", null);
            while (rs.next()) {
                String uTable = rs.getString("TABLE_NAME");
                System.out.println("Table '" + uTable + "' exists! Columns:");
                ResultSet cols = dbmd.getColumns(null, null, uTable, null);
                while (cols.next()) {
                    System.out.println("   * " + cols.getString("COLUMN_NAME") + " (" + cols.getString("TYPE_NAME") + ")");
                }
            }

            Conexion.cerrarConexion();
        } catch (Exception e) {
            System.err.println("DB Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

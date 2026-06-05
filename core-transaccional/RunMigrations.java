import java.sql.*;
import java.nio.file.*;
public class RunMigrations {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/core_transaccional_db", "postgres", "12345");
        Statement stmt = conn.createStatement();
        String v1 = new String(Files.readAllBytes(Paths.get("src/main/resources/db/migration/V1__init_schema_viajes.sql")));
        String v2 = new String(Files.readAllBytes(Paths.get("src/main/resources/db/migration/V2__insertar_datos_catalogo.sql")));
        
        stmt.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        stmt.execute(v1);
        stmt.execute(v2);
        System.out.println("Migrations run successfully!");
    }
}

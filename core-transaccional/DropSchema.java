import java.sql.*;
public class DropSchema {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/core_transaccional_db", "postgres", "12345");
        Statement stmt = conn.createStatement();
        stmt.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        System.out.println("Schema dropped!");
    }
}

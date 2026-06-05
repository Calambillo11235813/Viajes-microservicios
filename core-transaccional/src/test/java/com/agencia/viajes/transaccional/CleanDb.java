package com.agencia.viajes.transaccional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CleanDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/core_transaccional_db";
        try (Connection conn = DriverManager.getConnection(url, "postgres", "12345");
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
            System.out.println("Schema public dropped and recreated.");
        }
    }
}

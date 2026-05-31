package com.agencia.viajes.transaccional;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoreTransaccionalApplication {

	public static void main(String[] args) {
		try {
			Flyway flyway = Flyway.configure()
					.dataSource("jdbc:postgresql://localhost:5432/core_transaccional_db", "postgres", "nicolas123")
					.locations("classpath:db/migration")
					.load();
			flyway.migrate();
			System.out.println("FLYWAY MIGRATION SUCCESSFUL!");
		} catch (Exception e) {
			System.err.println("FLYWAY MIGRATION FAILED: " + e.getMessage());
			e.printStackTrace();
		}

		SpringApplication.run(CoreTransaccionalApplication.class, args);
	}

}

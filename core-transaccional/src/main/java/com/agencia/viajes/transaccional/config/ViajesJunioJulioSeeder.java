package com.agencia.viajes.transaccional.config;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@Order(2)
@RequiredArgsConstructor
public class ViajesJunioJulioSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        // Verificar si ya existen viajes en junio/julio de 2026 para evitar duplicados
        Integer viajesExistentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM VIAJE_PROGRAMADO WHERE fecha_hora_salida >= '2026-06-01' AND fecha_hora_salida < '2026-08-01'", 
                Integer.class);
        
        if (viajesExistentes != null && viajesExistentes > 500) {
            System.out.println("Los viajes de junio y julio ya existen en la base de datos (" + viajesExistentes + "). Saltando seeder extra...");
            return;
        }

        System.out.println("Generando 10 viajes POR RUTA para cada día de junio y julio de 2026...");
        
        List<RouteCatalog> rutas = jdbcTemplate.query(
                "SELECT id_ruta, duracion_estimada_horas, precio_base FROM RUTA_DESTINO ORDER BY id_ruta",
                (rs, rowNum) -> new RouteCatalog(
                        rs.getLong("id_ruta"),
                        rs.getBigDecimal("duracion_estimada_horas"),
                        rs.getBigDecimal("precio_base")));

        List<FleetCatalog> flotas = jdbcTemplate.query(
                "SELECT id_bus, capacidad_total_asientos FROM FLOTA ORDER BY id_bus",
                (rs, rowNum) -> new FleetCatalog(
                        rs.getLong("id_bus"),
                        rs.getInt("capacidad_total_asientos")));

        if (rutas.isEmpty() || flotas.isEmpty()) {
            System.out.println("ADVERTENCIA: No hay rutas o flotas registradas en la BD. Se cancela la generacion de viajes.");
            return;
        }

        String sql = """
                INSERT INTO VIAJE_PROGRAMADO (
                    id_ruta,
                    id_bus,
                    fecha_hora_salida,
                    fecha_hora_llegada,
                    estado_viaje
                ) VALUES (?, ?, ?, ?, 'PROGRAMADO')
                """;

        List<Object[]> batch = new ArrayList<>();
        LocalDate fechaInicio = LocalDate.of(2026, 6, 1);
        LocalDate fechaFin = LocalDate.of(2026, 7, 31);

        // 1. Recorrer todos los dias de junio y julio
        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            
            // 2. Recorrer TODAS las rutas disponibles
            for (RouteCatalog ruta : rutas) {
                
                // 3. Generar exactamente 10 viajes para esta ruta especifica en este dia
                for (int i = 0; i < 10; i++) { 
                    FleetCatalog flota = flotas.get(random.nextInt(flotas.size()));
                    
                    // Hora aleatoria entre 06:00 y 22:00
                    LocalTime horaSalida = LocalTime.of(6 + random.nextInt(16), random.nextInt(60));
                    LocalDateTime salida = LocalDateTime.of(fecha, horaSalida);
                    
                    // Calculo de llegada basado en la BD
                    long minutosDuracion = ruta.duracionHoras().multiply(BigDecimal.valueOf(60)).longValue();
                    LocalDateTime llegada = salida.plusMinutes(minutosDuracion);

                    batch.add(new Object[]{
                            ruta.id(),
                            flota.id(),
                            Timestamp.valueOf(salida),
                            Timestamp.valueOf(llegada)
                    });
                }
            }
        }

        // Ejecutar el lote completo
        jdbcTemplate.batchUpdate(sql, batch);
        System.out.println("¡Exito! Se generaron e insertaron " + batch.size() + " viajes para junio y julio.");
    }

    private record RouteCatalog(long id, BigDecimal duracionHoras, BigDecimal precioBase) {}
    private record FleetCatalog(long id, int capacidadTotalAsientos) {}
}

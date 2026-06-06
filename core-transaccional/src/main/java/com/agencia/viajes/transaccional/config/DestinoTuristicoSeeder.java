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
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Seeder que garantiza la existencia de:
 * 1. Destinos turísticos reconocidos por la IA (tabla DESTINO_TURISTICO).
 * 2. Rutas desde las ciudades principales hacia cada departamento destino.
 * 3. Viajes programados futuros para dichas rutas (junio-julio 2026).
 *
 * <p>Diseñado para ser idempotente: verifica antes de insertar.</p>
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class DestinoTuristicoSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    // ─── Catálogo de destinos turísticos reconocidos por la IA ───
    private static final List<DestinoIA> DESTINOS_IA = List.of(
            new DestinoIA("Uyuni", "Potosi",
                    "Salar de Uyuni, el mayor desierto de sal continuo y alto del mundo."),
            new DestinoIA("Samaipata", "Santa Cruz",
                    "Conocido por El Fuerte de Samaipata, un sitio arqueológico precolombino Patrimonio de la Humanidad."),
            new DestinoIA("Cristo_ConCordia", "Cochabamba",
                    "Estatua monumental de Jesucristo ubicada en el cerro San Pedro, la más alta de Sudamérica.")
    );

    /**
     * Rutas adicionales necesarias para que los destinos turísticos
     * sean accesibles desde las principales ciudades de Bolivia.
     *
     * Cada entrada: (origen, destino/departamento, duración en horas, categoría, precio).
     * Se insertan SOLO si la combinación origen-destino no existe aún.
     */
    private static final List<RutaNueva> RUTAS_NUEVAS = List.of(
            // ── Rutas hacia Potosí (para Uyuni) ──
            new RutaNueva("Santa Cruz", "Potosi", 20.00, "Normal", 200.00),
            new RutaNueva("Santa Cruz", "Potosi", 19.50, "Cama VIP", 250.00),
            new RutaNueva("Cochabamba", "Potosi", 10.00, "Normal", 150.00),
            new RutaNueva("Tarija",     "Potosi", 11.00, "Sur",       160.00),

            // ── Rutas hacia Santa Cruz (para Samaipata) ──
            // Actualmente solo SALEN de Santa Cruz, no LLEGAN. Agregar inversas.
            new RutaNueva("Cochabamba",  "Santa Cruz",  9.00, "Interdepartamental", 135.00),
            new RutaNueva("La Paz",      "Santa Cruz", 17.00, "Altiplano",          240.00),
            new RutaNueva("Sucre",       "Santa Cruz", 11.50, "Historico",          170.00),

            // ── Rutas hacia Cochabamba (para Cristo ConCordia) ──
            // Ya existen: Santa Cruz→Cochabamba (135), La Paz→Cochabamba (95)
            // Faltan:
            new RutaNueva("Sucre",  "Cochabamba", 7.00, "Valle",     100.00),
            new RutaNueva("Oruro",  "Cochabamba", 5.00, "Altiplano",  75.00),
            new RutaNueva("Tarija", "Cochabamba", 12.50, "Sur",       175.00)
    );

    @Override
    public void run(String... args) {
        seedDestinosTuristicos();
        List<Long> nuevasRutaIds = seedRutasNuevas();
        if (!nuevasRutaIds.isEmpty()) {
            seedViajesProgramados(nuevasRutaIds);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  1. DESTINOS TURÍSTICOS
    // ═══════════════════════════════════════════════════════════════
    private void seedDestinosTuristicos() {
        Integer existentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DESTINO_TURISTICO", Integer.class);

        if (existentes != null && existentes >= DESTINOS_IA.size()) {
            System.out.println("[DestinoSeeder] Destinos turísticos ya existen (" + existentes + "). Saltando...");
            return;
        }

        String sql = """
                INSERT INTO DESTINO_TURISTICO (nombre_turistico, departamento, descripcion)
                VALUES (?, ?, ?)
                ON CONFLICT (nombre_turistico) DO NOTHING
                """;

        for (DestinoIA destino : DESTINOS_IA) {
            jdbcTemplate.update(sql, destino.nombre(), destino.departamento(), destino.descripcion());
        }

        System.out.println("[DestinoSeeder] ✅ Insertados " + DESTINOS_IA.size() + " destinos turísticos.");
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. RUTAS NUEVAS
    // ═══════════════════════════════════════════════════════════════
    private List<Long> seedRutasNuevas() {
        List<Long> insertedIds = new ArrayList<>();

        for (RutaNueva ruta : RUTAS_NUEVAS) {
            // Verificar si ya existe esta combinación origen→destino
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM RUTA_DESTINO WHERE LOWER(ciudad_origen) = LOWER(?) AND LOWER(ciudad_destino) = LOWER(?)",
                    Integer.class, ruta.origen(), ruta.destino());

            if (count != null && count > 0) {
                continue; // Ya existe, no duplicar
            }

            jdbcTemplate.update("""
                    INSERT INTO RUTA_DESTINO (ciudad_origen, ciudad_destino, duracion_estimada_horas, categoria_turistica, precio_base)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    ruta.origen(), ruta.destino(),
                    BigDecimal.valueOf(ruta.duracion()),
                    ruta.categoria(),
                    BigDecimal.valueOf(ruta.precio()));

            // Recuperar el ID generado
            Long idGenerado = jdbcTemplate.queryForObject(
                    "SELECT MAX(id_ruta) FROM RUTA_DESTINO", Long.class);
            if (idGenerado != null) {
                insertedIds.add(idGenerado);
            }

            System.out.println("[DestinoSeeder] ✅ Nueva ruta: " + ruta.origen() + " → " + ruta.destino()
                    + " (Bs " + ruta.precio() + ")");
        }

        if (insertedIds.isEmpty()) {
            System.out.println("[DestinoSeeder] Todas las rutas necesarias ya existen. Saltando...");
        }

        return insertedIds;
    }

    // ═══════════════════════════════════════════════════════════════
    //  3. VIAJES PROGRAMADOS PARA LAS NUEVAS RUTAS
    // ═══════════════════════════════════════════════════════════════
    private void seedViajesProgramados(List<Long> rutaIds) {
        // Cargar flotas disponibles
        List<FlotaInfo> flotas = jdbcTemplate.query(
                "SELECT id_bus, capacidad_total_asientos FROM FLOTA ORDER BY id_bus",
                (rs, rowNum) -> new FlotaInfo(rs.getLong("id_bus"), rs.getInt("capacidad_total_asientos")));

        if (flotas.isEmpty()) {
            System.out.println("[DestinoSeeder] ⚠️ No hay flotas registradas. No se pueden crear viajes.");
            return;
        }

        // Cargar info de las rutas insertadas
        List<RutaInfo> rutasInsertadas = new ArrayList<>();
        for (Long rutaId : rutaIds) {
            jdbcTemplate.query(
                    "SELECT id_ruta, duracion_estimada_horas FROM RUTA_DESTINO WHERE id_ruta = ?",
                    (rs, rowNum) -> {
                        rutasInsertadas.add(new RutaInfo(
                                rs.getLong("id_ruta"),
                                rs.getBigDecimal("duracion_estimada_horas")));
                        return null;
                    },
                    rutaId);
        }

        String sql = """
                INSERT INTO VIAJE_PROGRAMADO (id_ruta, id_bus, fecha_hora_salida, fecha_hora_llegada, estado_viaje)
                VALUES (?, ?, ?, ?, 'PROGRAMADO')
                """;

        List<Object[]> batch = new ArrayList<>();
        LocalDate fechaInicio = LocalDate.of(2026, 6, 7); // Desde mañana (aprox)
        LocalDate fechaFin = LocalDate.of(2026, 7, 31);

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            for (RutaInfo ruta : rutasInsertadas) {
                // Generar 5 viajes por día por ruta nueva
                for (int i = 0; i < 5; i++) {
                    FlotaInfo flota = flotas.get(random.nextInt(flotas.size()));
                    LocalTime horaSalida = LocalTime.of(6 + random.nextInt(16), random.nextInt(60));
                    LocalDateTime salida = LocalDateTime.of(fecha, horaSalida);
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

        jdbcTemplate.batchUpdate(sql, batch);
        System.out.println("[DestinoSeeder] ✅ Generados " + batch.size()
                + " viajes programados para las nuevas rutas (junio-julio 2026).");
    }

    // ─── Records auxiliares ───
    private record DestinoIA(String nombre, String departamento, String descripcion) {}
    private record RutaNueva(String origen, String destino, double duracion, String categoria, double precio) {}
    private record FlotaInfo(long id, int capacidadTotalAsientos) {}
    private record RutaInfo(long id, BigDecimal duracionHoras) {}
}

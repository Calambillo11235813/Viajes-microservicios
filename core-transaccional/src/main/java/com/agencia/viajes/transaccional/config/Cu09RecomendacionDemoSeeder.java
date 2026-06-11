package com.agencia.viajes.transaccional.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeder de demostración para CU-09 (recomendación personalizada del motor IA).
 * Crea tres usuarios con historiales de viaje distintos (perfil y categoría preferida).
 * Solo se ejecuta una vez al arrancar, si los usuarios demo aún no existen.
 */
@Component
@Profile("!test")
@Order(101)
@RequiredArgsConstructor
public class Cu09RecomendacionDemoSeeder implements CommandLineRunner {

    private static final String ESTADO_VIAJE_COMPLETADO = "COMPLETADO";
    private static final String ESTADO_RESERVA_COMPLETADA = "COMPLETADA";
    private static final String ESTADO_PAGO_CONFIRMADO = "CONFIRMADO";
    private static final String ESTADO_BOLETO_USADO = "USADO";
    private static final String TIPO_PASAJERO_ADULTO = "ADULTO";
    private static final String NOMBRE_PASAJERO_DEMO = "Pasajero CU09 Demo";

    private static final List<String> EMAILS_DEMO = List.of(
            "nicolas@viajes.com",
            "klaus@viajes.com",
            "sebastian@viajes.com");

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${demo.cu09.nicolas.reservations:28}")
    private int reservasNicolas;

    @Value("${demo.cu09.klaus.reservations:10}")
    private int reservasKlaus;

    @Value("${demo.cu09.sebastian.reservations:16}")
    private int reservasSebastian;

    @Override
    @Transactional
    public void run(String... args) {
        Integer usuariosExistentes = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM USUARIO
                WHERE LOWER(email) IN (?, ?, ?)
                """,
                Integer.class,
                EMAILS_DEMO.get(0),
                EMAILS_DEMO.get(1),
                EMAILS_DEMO.get(2));

        if (usuariosExistentes != null && usuariosExistentes > 0) {
            System.out.printf(
                    Locale.ROOT,
                    "[Cu09RecomendacionDemoSeeder] Usuarios demo CU-09 ya existen (%d); se omite la carga.%n",
                    usuariosExistentes);
            return;
        }

        Integer idRolCliente = jdbcTemplate.queryForObject(
                "SELECT id_rol FROM ROL WHERE UPPER(nombre) = 'CLIENTE'",
                Integer.class);

        List<RouteSeed> rutas = cargarRutas();
        List<FleetSeed> flotas = cargarFlotas();
        if (rutas.isEmpty() || flotas.isEmpty()) {
            throw new IllegalStateException("No hay rutas o flota disponibles para el seeder CU-09.");
        }

        Integer idNicolas = crearUsuario(
                idRolCliente,
                "Nicolas Mendoza",
                "nicolas@viajes.com",
                "nicolas123",
                "70110001",
                "4521001-SC");
        Integer idKlaus = crearUsuario(
                idRolCliente,
                "Klaus Rojas",
                "klaus@viajes.com",
                "klaus123",
                "70110002",
                "4521002-LP");
        Integer idSebastian = crearUsuario(
                idRolCliente,
                "Sebastian Vargas",
                "sebastian@viajes.com",
                "sebastian123",
                "70110003",
                "4521003-CB");

        List<RouteSeed> rutasAltiplano = filtrarPorCategoria(rutas, "Altiplano");
        List<RouteSeed> rutasSur = filtrarPorCategoria(rutas, "Sur");
        List<RouteSeed> rutasHistoricoValle = rutas.stream()
                .filter(r -> "Historico".equals(r.categoria()) || "Valle".equals(r.categoria()))
                .toList();

        Random randomNicolas = new Random(20260611L);
        Random randomKlaus = new Random(20260612L);
        Random randomSebastian = new Random(20260613L);

        sembrarHistorial(
                idNicolas,
                "Nicolas",
                reservasNicolas,
                rutasAltiplano,
                rutas,
                randomNicolas,
                0.85,
                2,
                4,
                flotas);
        sembrarHistorial(
                idKlaus,
                "Klaus",
                reservasKlaus,
                rutasSur,
                rutas,
                randomKlaus,
                0.80,
                1,
                1,
                flotas);
        sembrarHistorial(
                idSebastian,
                "Sebastian",
                reservasSebastian,
                rutasHistoricoValle,
                rutas,
                randomSebastian,
                0.75,
                1,
                2,
                flotas);

        System.out.println("[Cu09RecomendacionDemoSeeder] Usuarios listos para CU-09:");
        System.out.println("  - nicolas@viajes.com / nicolas123  -> perfil Premium, categoria Altiplano");
        System.out.println("  - klaus@viajes.com / klaus123      -> perfil Economico, categoria Sur");
        System.out.println("  - sebastian@viajes.com / sebastian123 -> perfil Estandar, categoria Historico/Valle");
    }

    private Integer crearUsuario(
            Integer idRol,
            String nombreCompleto,
            String email,
            String passwordPlano,
            String telefono,
            String ciPasaporte) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO USUARIO (
                    ci_pasaporte,
                    nombre_completo,
                    email,
                    password_hash,
                    telefono,
                    id_rol
                ) VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_usuario
                """,
                Integer.class,
                ciPasaporte,
                nombreCompleto,
                email,
                passwordEncoder.encode(passwordPlano),
                telefono,
                idRol);
    }

    private void sembrarHistorial(
            Integer idUsuario,
            String etiqueta,
            int cantidadReservas,
            List<RouteSeed> rutasPreferidas,
            List<RouteSeed> rutasTotales,
            Random random,
            double pesoPreferida,
            int minPasajeros,
            int maxPasajeros,
            List<FleetSeed> flotas) {
        LocalDateTime ahora = LocalDateTime.now().withNano(0);

        for (int i = 0; i < cantidadReservas; i++) {
            RouteSeed ruta = elegirRuta(rutasPreferidas, rutasTotales, random, pesoPreferida);
            FleetSeed flota = flotas.get(random.nextInt(flotas.size()));

            LocalDateTime salida = ahora.minusDays(3L + i).minusHours(random.nextInt(8));
            LocalDateTime llegada = salida.plusMinutes(calcularDuracionMinutos(ruta.duracionHoras()));
            LocalDateTime fechaCreacion = salida.minusDays(2 + random.nextInt(5));
            LocalDateTime fechaPago = fechaCreacion.plusMinutes(15 + random.nextInt(30));

            int cantidadPasajeros = minPasajeros + random.nextInt(maxPasajeros - minPasajeros + 1);
            BigDecimal monto = ruta.precioBase()
                    .multiply(BigDecimal.valueOf(cantidadPasajeros))
                    .setScale(2, RoundingMode.HALF_UP);

            Integer idViaje = crearViaje(ruta.idRuta(), flota.idBus(), salida, llegada);
            Integer idReserva = crearReserva(idUsuario, idViaje, fechaCreacion, monto, cantidadPasajeros);
            crearPago(idReserva, fechaPago, monto);
            crearBoleto(idReserva, fechaPago.plusMinutes(5), flota.capacidadTotalAsientos(), i, etiqueta);
        }
    }

    private RouteSeed elegirRuta(
            List<RouteSeed> preferidas,
            List<RouteSeed> todas,
            Random random,
            double pesoPreferida) {
        List<RouteSeed> poolPreferido = preferidas.isEmpty() ? todas : preferidas;
        if (random.nextDouble() < pesoPreferida) {
            return poolPreferido.get(random.nextInt(poolPreferido.size()));
        }
        return todas.get(random.nextInt(todas.size()));
    }

    private List<RouteSeed> filtrarPorCategoria(List<RouteSeed> rutas, String categoria) {
        return rutas.stream().filter(r -> categoria.equals(r.categoria())).toList();
    }

    private List<RouteSeed> cargarRutas() {
        return jdbcTemplate.query(
                """
                SELECT id_ruta, categoria_turistica, precio_base, duracion_estimada_horas
                FROM RUTA_DESTINO
                ORDER BY id_ruta
                """,
                (rs, rowNum) -> new RouteSeed(
                        rs.getInt("id_ruta"),
                        rs.getString("categoria_turistica"),
                        rs.getBigDecimal("precio_base"),
                        rs.getBigDecimal("duracion_estimada_horas")));
    }

    private List<FleetSeed> cargarFlotas() {
        return jdbcTemplate.query(
                """
                SELECT id_bus, capacidad_total_asientos
                FROM FLOTA
                ORDER BY id_bus
                """,
                (rs, rowNum) -> new FleetSeed(
                        rs.getInt("id_bus"),
                        rs.getInt("capacidad_total_asientos")));
    }

    private Integer crearViaje(Integer idRuta, Integer idBus, LocalDateTime salida, LocalDateTime llegada) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO VIAJE_PROGRAMADO (
                    id_ruta, id_bus, fecha_hora_salida, fecha_hora_llegada, estado_viaje
                ) VALUES (?, ?, ?, ?, ?)
                RETURNING id_viaje
                """,
                Integer.class,
                idRuta,
                idBus,
                Timestamp.valueOf(salida),
                Timestamp.valueOf(llegada),
                ESTADO_VIAJE_COMPLETADO);
    }

    private Integer crearReserva(
            Integer idUsuario,
            Integer idViaje,
            LocalDateTime fechaCreacion,
            BigDecimal monto,
            int cantidadPasajeros) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO RESERVA (
                    id_usuario, id_viaje, fecha_creacion, estado_reserva,
                    monto_total_pagado, cantidad_pasajeros
                ) VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_reserva
                """,
                Integer.class,
                idUsuario,
                idViaje,
                Timestamp.valueOf(fechaCreacion),
                ESTADO_RESERVA_COMPLETADA,
                monto,
                cantidadPasajeros);
    }

    private void crearPago(Integer idReserva, LocalDateTime fechaPago, BigDecimal monto) {
        jdbcTemplate.update(
                """
                INSERT INTO PAGO (
                    id_reserva, fecha_pago, monto_transaccion, metodo_pago_usado, estado_pago
                ) VALUES (?, ?, ?, ?, ?)
                """,
                idReserva,
                Timestamp.valueOf(fechaPago),
                monto,
                "QR",
                ESTADO_PAGO_CONFIRMADO);
    }

    private void crearBoleto(
            Integer idReserva,
            LocalDateTime fechaEmision,
            int capacidadTotalAsientos,
            int indice,
            String etiqueta) {
        int asiento = 1 + (indice % Math.max(1, capacidadTotalAsientos));
        jdbcTemplate.update(
                """
                INSERT INTO BOLETO_ASIENTO (
                    id_reserva, numero_asiento, nombre_pasajero, hash_blockchain,
                    codigo_qr, fecha_emision, estado_boleto, tipo_pasajero
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idReserva,
                String.valueOf(asiento),
                NOMBRE_PASAJERO_DEMO + " " + etiqueta,
                generarHashSeguro(),
                generarCodigoQr(),
                Timestamp.valueOf(fechaEmision),
                ESTADO_BOLETO_USADO,
                TIPO_PASAJERO_ADULTO);
    }

    private long calcularDuracionMinutos(BigDecimal duracionHoras) {
        return duracionHoras.multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private String generarHashSeguro() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    private String generarCodigoQr() {
        return "QR-CU09-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase(Locale.ROOT);
    }

    private record RouteSeed(
            Integer idRuta,
            String categoria,
            BigDecimal precioBase,
            BigDecimal duracionHoras) {
    }

    private record FleetSeed(Integer idBus, int capacidadTotalAsientos) {
    }
}

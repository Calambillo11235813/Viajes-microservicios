package com.agencia.viajes.transaccional.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
 * Seeder de demostración para generar viajes completados calificables en la app móvil.
 * Se ejecuta al arrancar Spring Boot y solo inserta si aún no existen datos de demo
 * (mismo criterio que {@link DatabaseSeeder}).
 */
@Component
@Profile("!test")
@Order(100)
@RequiredArgsConstructor
public class DemoFeedbackSeeder implements CommandLineRunner {

    private static final String ESTADO_VIAJE_COMPLETADO = "COMPLETADO";
    private static final String ESTADO_RESERVA_COMPLETADA = "COMPLETADA";
    private static final String ESTADO_PAGO_CONFIRMADO = "CONFIRMADO";
    private static final String ESTADO_BOLETO_USADO = "USADO";
    private static final String TIPO_PASAJERO_ADULTO = "ADULTO";
    private static final String NOMBRE_PASAJERO_DEMO = "Cliente Demo Feedback";

    private final JdbcTemplate jdbcTemplate;

    @Value("${demo.feedback.seed.email:cliente@viajes.com}")
    private String emailCliente;

    @Value("${demo.feedback.seed.count:200}")
    private int cantidadViajes;

    @Override
    @Transactional
    public void run(String... args) {
        Integer idUsuario = buscarIdUsuario(emailCliente);

        Integer reservasDemoExistentes = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT r.id_reserva)
                FROM RESERVA r
                JOIN BOLETO_ASIENTO b ON b.id_reserva = r.id_reserva
                WHERE r.id_usuario = ?
                  AND r.estado_reserva = ?
                  AND b.nombre_pasajero = ?
                """,
                Integer.class,
                idUsuario,
                ESTADO_RESERVA_COMPLETADA,
                NOMBRE_PASAJERO_DEMO);

        if (reservasDemoExistentes != null && reservasDemoExistentes > 0) {
            System.out.printf(
                    Locale.ROOT,
                    "[DemoFeedbackSeeder] Ya existen %d reservas demo para %s; se omite la carga.%n",
                    reservasDemoExistentes,
                    emailCliente);
            return;
        }

        List<RouteSeed> rutas = cargarRutas();
        List<FleetSeed> flotas = cargarFlotas();

        if (rutas.isEmpty()) {
            throw new IllegalStateException("No existen rutas en RUTA_DESTINO para crear viajes demo.");
        }
        if (flotas.isEmpty()) {
            throw new IllegalStateException("No existen buses en FLOTA para crear viajes demo.");
        }

        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        for (int i = 0; i < cantidadViajes; i++) {
            RouteSeed ruta = rutas.get(i % rutas.size());
            FleetSeed flota = flotas.get(i % flotas.size());

            LocalDateTime salida = ahora.minusDays(1 + (i / 8)).minusMinutes((long) (i % 8) * 20);
            LocalDateTime llegada = salida.plusMinutes(calcularDuracionMinutos(ruta.duracionHoras()));
            if (llegada.isAfter(ahora.minusMinutes(5))) {
                llegada = ahora.minusMinutes(5 + i);
            }

            // Reciente para que el historial paginado de la app muestre primero los datos de demo.
            LocalDateTime fechaCreacion = ahora.minusMinutes(i);
            LocalDateTime fechaPago = fechaCreacion.plusSeconds(30);
            int cantidadPasajeros = 1;
            BigDecimal monto = ruta.precioBase().multiply(BigDecimal.valueOf(cantidadPasajeros))
                    .setScale(2, RoundingMode.HALF_UP);

            Integer idViaje = crearViaje(ruta.idRuta(), flota.idBus(), salida, llegada);
            Integer idReserva = crearReserva(idUsuario, idViaje, fechaCreacion, monto, cantidadPasajeros);
            crearPago(idReserva, fechaPago, monto);
            crearBoleto(idReserva, fechaPago.plusSeconds(30), flota.capacidadTotalAsientos(), i);
        }

        System.out.printf(
                Locale.ROOT,
                "[DemoFeedbackSeeder] Creados %d viajes COMPLETADOS para %s (id_usuario=%d).%n",
                cantidadViajes,
                emailCliente,
                idUsuario);
    }

    private Integer buscarIdUsuario(String email) {
        List<Integer> ids = jdbcTemplate.query(
                "SELECT id_usuario FROM USUARIO WHERE LOWER(email) = LOWER(?)",
                (rs, rowNum) -> rs.getInt("id_usuario"),
                email);

        if (ids.isEmpty()) {
            throw new IllegalStateException("No existe usuario con email " + email);
        }
        return ids.get(0);
    }

    private List<RouteSeed> cargarRutas() {
        return jdbcTemplate.query(
                """
                SELECT id_ruta, duracion_estimada_horas, precio_base
                FROM RUTA_DESTINO
                ORDER BY id_ruta
                """,
                (rs, rowNum) -> new RouteSeed(
                        rs.getInt("id_ruta"),
                        rs.getBigDecimal("duracion_estimada_horas"),
                        rs.getBigDecimal("precio_base")));
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
                    id_ruta,
                    id_bus,
                    fecha_hora_salida,
                    fecha_hora_llegada,
                    estado_viaje
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
                    id_usuario,
                    id_viaje,
                    fecha_creacion,
                    estado_reserva,
                    monto_total_pagado,
                    cantidad_pasajeros
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
                    id_reserva,
                    fecha_pago,
                    monto_transaccion,
                    metodo_pago_usado,
                    estado_pago
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
            int indice) {
        int asiento = 1 + (indice % Math.max(1, capacidadTotalAsientos));
        jdbcTemplate.update(
                """
                INSERT INTO BOLETO_ASIENTO (
                    id_reserva,
                    numero_asiento,
                    nombre_pasajero,
                    hash_blockchain,
                    codigo_qr,
                    fecha_emision,
                    estado_boleto,
                    tipo_pasajero
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                idReserva,
                String.valueOf(asiento),
                NOMBRE_PASAJERO_DEMO,
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
        return "QR-DEMO-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase(Locale.ROOT);
    }

    private record RouteSeed(Integer idRuta, BigDecimal duracionHoras, BigDecimal precioBase) {
    }

    private record FleetSeed(Integer idBus, int capacidadTotalAsientos) {
    }
}

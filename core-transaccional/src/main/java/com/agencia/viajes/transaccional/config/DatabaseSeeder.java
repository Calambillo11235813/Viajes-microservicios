package com.agencia.viajes.transaccional.config;

import com.github.javafaker.Faker;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

@Component
@Profile("!test")
@Order(1)
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final int CHUNK_SIZE = 1000;
    private static final int TOTAL_USUARIOS = 1000;
    private static final int TOTAL_VIAJES = 5000;
    private static final int TOTAL_RESERVAS = 15000;
    private static final List<String> EXTENSIONES_CI = List.of(
            "SC", "LP", "CB", "OR", "CH", "TJ", "PT", "BE", "PD", "BN");
    private static final List<String> METODOS_PAGO = List.of("QR", "TRANSFERENCIA");
    private static final List<String> TIPOS_PASAJERO = List.of("ADULTO", "MENOR", "TERCERA_EDAD");
    private static final String ESTADO_VIAJE_PROGRAMADO = "PROGRAMADO";
    private static final String ESTADO_RESERVA_COMPLETADA = "COMPLETADA";
    private static final String ESTADO_RESERVA_CANCELADA = "CANCELADA";
    private static final String ESTADO_PAGO_CONFIRMADO = "CONFIRMADO";
    private static final String ESTADO_PAGO_REEMBOLSADO = "REEMBOLSADO";
    private static final String ESTADO_BOLETO_USADO = "USADO";
    private static final String ESTADO_BOLETO_ANULADO = "ANULADO";

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker(new Locale("es", "BO"));
    private final Random random = new Random();
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Integer usuariosExistentes = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USUARIO", Integer.class);
        if (usuariosExistentes != null && usuariosExistentes > 0) {
            return;
        }

        Map<String, RoleCatalog> roles = cargarRoles();
        RoleCatalog rolCliente = roles.get("CLIENTE");
        RoleCatalog rolAdmin = roles.get("ADMINISTRADOR");
        RoleCatalog rolGerente = roles.get("GERENTE");

        if (rolCliente == null || rolAdmin == null || rolGerente == null) {
            throw new IllegalStateException("Faltan roles requeridos (CLIENTE, ADMINISTRADOR, GERENTE) en la base de datos.");
        }

        List<RouteCatalog> rutas = cargarRutas();
        List<FleetCatalog> flotas = cargarFlotas();
        List<PackageCatalog> paquetes = cargarPaquetes();

        insertarUsuarios(rolCliente.id(), rolAdmin.id(), rolGerente.id());
        List<TripSeed> viajes = insertarViajes(rutas, flotas);
        insertarReservasPagosYBoletos(viajes, paquetes);
        reajustarSecuencias();
    }

    private Map<String, RoleCatalog> cargarRoles() {
        List<RoleCatalog> roles = jdbcTemplate.query(
                "SELECT id_rol, nombre FROM ROL ORDER BY id_rol",
                (rs, rowNum) -> new RoleCatalog(rs.getLong("id_rol"), rs.getString("nombre")));

        Map<String, RoleCatalog> rolesPorNombre = new HashMap<>();
        for (RoleCatalog role : roles) {
            rolesPorNombre.put(role.nombre().toUpperCase(Locale.ROOT), role);
        }
        return rolesPorNombre;
    }

    private List<RouteCatalog> cargarRutas() {
        return jdbcTemplate.query(
                "SELECT id_ruta, duracion_estimada_horas, precio_base FROM RUTA_DESTINO ORDER BY id_ruta",
                (rs, rowNum) -> new RouteCatalog(
                        rs.getLong("id_ruta"),
                        rs.getBigDecimal("duracion_estimada_horas"),
                        rs.getBigDecimal("precio_base")));
    }

    private List<FleetCatalog> cargarFlotas() {
        return jdbcTemplate.query(
                "SELECT id_bus, capacidad_total_asientos FROM FLOTA ORDER BY id_bus",
                (rs, rowNum) -> new FleetCatalog(
                        rs.getLong("id_bus"),
                        rs.getInt("capacidad_total_asientos")));
    }

        private List<PackageCatalog> cargarPaquetes() {
        return jdbcTemplate.query(
            "SELECT id_paquete, precio_total FROM PAQUETE_TURISTICO ORDER BY id_paquete",
            (rs, rowNum) -> new PackageCatalog(
                rs.getLong("id_paquete"),
                rs.getBigDecimal("precio_total")));
        }

    private void insertarUsuarios(long idRolCliente, long idRolAdmin, long idRolGerente) {
        String sql = """
                INSERT INTO USUARIO (
                    id_usuario,
                    ci_pasaporte,
                    nombre_completo,
                    email,
                    password_hash,
                    telefono,
                    id_rol
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        List<Object[]> batch = new ArrayList<>(CHUNK_SIZE);
        Set<String> ciGenerados = new HashSet<>();
        Set<String> emailsGenerados = new HashSet<>();

        // USUARIO TEST ESTÁTICO PARA PODER LOGUEARNOS EN LA APP MÓVIL
        batch.add(new Object[] {
            1, "1234567-SC", "Usuario Prueba API", "test@test.com", passwordEncoder.encode("123456"), "77766655", idRolCliente
        });
        ciGenerados.add("1234567-SC");
        emailsGenerados.add("test@test.com");

        // USUARIOS ESTÁTICOS PARA EL FRONTEND ADMINISTRATIVO
        batch.add(new Object[] {
            2, "9999999-SC", "Administrador Principal", "admin@viajes.com", passwordEncoder.encode("admin123"), "70000001", idRolAdmin
        });
        ciGenerados.add("9999999-SC");
        emailsGenerados.add("admin@viajes.com");

        batch.add(new Object[] {
            3, "8888888-LP", "Gerente General", "gerente@viajes.com", passwordEncoder.encode("gerente123"), "70000002", idRolGerente
        });
        ciGenerados.add("8888888-LP");
        emailsGenerados.add("gerente@viajes.com");

        for (int i = 4; i <= TOTAL_USUARIOS; i++) {
            String nombreCompleto = faker.name().fullName();
            String email = generarEmail(nombreCompleto, i, emailsGenerados);
            String ciPasaporte = generarCiBoliviano(ciGenerados);

            batch.add(new Object[]{
                    i,
                    ciPasaporte,
                    nombreCompleto,
                    email,
                    generarHashSeguro(),
                    generarTelefonoBoliviano(),
                    idRolCliente
            });

            if (batch.size() == CHUNK_SIZE) {
                ejecutarBatch(sql, batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            ejecutarBatch(sql, batch);
        }
    }

    private List<TripSeed> insertarViajes(List<RouteCatalog> rutas, List<FleetCatalog> flotas) {
        String sql = """
                INSERT INTO VIAJE_PROGRAMADO (
                    id_viaje,
                    id_ruta,
                    id_bus,
                    fecha_hora_salida,
                    fecha_hora_llegada,
                    estado_viaje
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        List<TripSeed> viajes = new ArrayList<>(TOTAL_VIAJES);
        List<Object[]> batch = new ArrayList<>(CHUNK_SIZE);
        LocalDateTime ahora = LocalDateTime.now();

        for (int i = 1; i <= TOTAL_VIAJES; i++) {
            RouteCatalog ruta = rutas.get(random.nextInt(rutas.size()));
            FleetCatalog flota = flotas.get(random.nextInt(flotas.size()));
            LocalDateTime salida = generarFechaAleatoriaUltimos12Meses(ahora);
            LocalDateTime llegada = calcularLlegada(salida, ruta.duracionHoras());

            viajes.add(new TripSeed(i, ruta.id(), flota.id(), salida, llegada, ruta.precioBase(), flota.capacidadTotalAsientos()));
            batch.add(new Object[]{
                    i,
                    ruta.id(),
                    flota.id(),
                    Timestamp.valueOf(salida),
                    Timestamp.valueOf(llegada),
                    ESTADO_VIAJE_PROGRAMADO
            });

            if (batch.size() == CHUNK_SIZE) {
                ejecutarBatch(sql, batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            ejecutarBatch(sql, batch);
        }

        return viajes;
    }

    private void insertarReservasPagosYBoletos(List<TripSeed> viajes, List<PackageCatalog> paquetes) {
        String reservaSql = """
                INSERT INTO RESERVA (
                    id_reserva,
                    id_usuario,
                    id_viaje,
                    id_paquete,
                    fecha_creacion,
                    estado_reserva,
                    monto_total_pagado,
                    cantidad_pasajeros
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String pagoSql = """
                INSERT INTO PAGO (
                    id_pago,
                    id_reserva,
                    fecha_pago,
                    monto_transaccion,
                    metodo_pago_usado,
                    cupon_descuento_aplicado,
                    estado_pago
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String boletoSql = """
                INSERT INTO BOLETO_ASIENTO (
                    id_boleto,
                    id_reserva,
                    numero_asiento,
                    nombre_pasajero,
                    hash_blockchain,
                    codigo_qr,
                    fecha_emision,
                    estado_boleto,
                    tipo_pasajero
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        List<Object[]> reservasBatch = new ArrayList<>(CHUNK_SIZE);
        List<Object[]> pagosBatch = new ArrayList<>(CHUNK_SIZE);
        List<Object[]> boletosBatch = new ArrayList<>(CHUNK_SIZE * 2);
        long boletoId = 1L;

        for (int reservaId = 1; reservaId <= TOTAL_RESERVAS; reservaId++) {
            TripSeed viaje = viajes.get(random.nextInt(viajes.size()));
            int idUsuario = 1 + random.nextInt(TOTAL_USUARIOS);
            boolean esCompletada = random.nextInt(100) < 80;
            String estadoReserva = esCompletada ? ESTADO_RESERVA_COMPLETADA : ESTADO_RESERVA_CANCELADA;
            int cantidadPasajeros = 1 + random.nextInt(3);
            PackageCatalog paquete = seleccionarPaqueteAleatorio(paquetes, reservaId);
            BigDecimal montoTransaccion = calcularMontoReserva(viaje.precioBase(), paquete, cantidadPasajeros);
            LocalDateTime fechaCreacion = generarFechaReservaCoherente(viaje.fechaSalida());
            LocalDateTime fechaPago = fechaCreacion.plusMinutes(10L + random.nextInt(50));
            String cupon = generarCuponSiCorresponde(reservaId, esCompletada, paquete);

            reservasBatch.add(new Object[]{
                    reservaId,
                    idUsuario,
                    viaje.id(),
                paquete != null ? paquete.id() : null,
                    Timestamp.valueOf(fechaCreacion),
                    estadoReserva,
                    montoTransaccion,
                    cantidadPasajeros
            });

            pagosBatch.add(new Object[]{
                    reservaId,
                    reservaId,
                    Timestamp.valueOf(fechaPago),
                    montoTransaccion,
                    METODOS_PAGO.get(random.nextInt(METODOS_PAGO.size())),
                        cupon,
                    esCompletada ? ESTADO_PAGO_CONFIRMADO : ESTADO_PAGO_REEMBOLSADO
            });

            Set<Integer> asientosUsados = new HashSet<>();
            for (int pasajero = 0; pasajero < cantidadPasajeros; pasajero++) {
                int numeroAsiento = generarAsientoDisponible(viaje.capacidadTotalAsientos(), asientosUsados);
                String nombrePasajero = faker.name().fullName();
                LocalDateTime fechaEmision = fechaPago.plusMinutes(1L + pasajero);

                boletosBatch.add(new Object[]{
                        boletoId++,
                        reservaId,
                        String.valueOf(numeroAsiento),
                        nombrePasajero,
                        generarHashSeguro(),
                        generarCodigoQr(),
                        Timestamp.valueOf(fechaEmision),
                        esCompletada ? ESTADO_BOLETO_USADO : ESTADO_BOLETO_ANULADO,
                        TIPOS_PASAJERO.get(random.nextInt(TIPOS_PASAJERO.size()))
                });
            }

            if (reservasBatch.size() == CHUNK_SIZE) {
                ejecutarBatch(reservaSql, reservasBatch);
                ejecutarBatch(pagoSql, pagosBatch);
                ejecutarBatch(boletoSql, boletosBatch);
                reservasBatch.clear();
                pagosBatch.clear();
                boletosBatch.clear();
            }
        }

        if (!reservasBatch.isEmpty()) {
            ejecutarBatch(reservaSql, reservasBatch);
            ejecutarBatch(pagoSql, pagosBatch);
            ejecutarBatch(boletoSql, boletosBatch);
        }
    }

    private void reajustarSecuencias() {
        ajustarSecuencia("USUARIO", "id_usuario");
        ajustarSecuencia("VIAJE_PROGRAMADO", "id_viaje");
        ajustarSecuencia("RESERVA", "id_reserva");
        ajustarSecuencia("PAGO", "id_pago");
        ajustarSecuencia("BOLETO_ASIENTO", "id_boleto");
    }

    private void ajustarSecuencia(String tabla, String columna) {
        try {
            String sql = "SELECT setval(pg_get_serial_sequence(?, ?), COALESCE((SELECT MAX(" + columna + ") FROM " + tabla + "), 1), true)";
            jdbcTemplate.queryForObject(sql, Long.class, tabla.toLowerCase(Locale.ROOT), columna);
        } catch (Exception e) {
            // Ignorado en H2 u otras bases de datos que no soportan secuencias de Postgres
        }
    }

    private void ejecutarBatch(String sql, List<Object[]> batch) {
        for (int inicio = 0; inicio < batch.size(); inicio += CHUNK_SIZE) {
            int fin = Math.min(inicio + CHUNK_SIZE, batch.size());
            jdbcTemplate.batchUpdate(sql, batch.subList(inicio, fin));
        }
    }

    private String generarCiBoliviano(Set<String> ciGenerados) {
        while (true) {
            String ci = String.format("%07d %s",
                    1_000_000 + random.nextInt(9_000_000),
                    EXTENSIONES_CI.get(random.nextInt(EXTENSIONES_CI.size())));
            if (ciGenerados.add(ci)) {
                return ci;
            }
        }
    }

    private String generarEmail(String nombreCompleto, int indice, Set<String> emailsGenerados) {
        String base = Normalizer.normalize(nombreCompleto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.|\\.$", "");
        String email = base + "." + String.format("%04d", indice) + "@correo.bo";
        if (emailsGenerados.add(email)) {
            return email;
        }
        String fallback = base + "." + indice + "." + UUID.randomUUID().toString().substring(0, 8) + "@correo.bo";
        emailsGenerados.add(fallback);
        return fallback;
    }

    private String generarTelefonoBoliviano() {
        int numero = 60_000_000 + random.nextInt(20_000_000);
        return String.valueOf(numero);
    }

    private String generarHashSeguro() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private String generarCodigoQr() {
        return "QR-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase(Locale.ROOT);
    }

    private LocalDateTime generarFechaAleatoriaUltimos12Meses(LocalDateTime ahora) {
        LocalDateTime inicio = ahora.minusMonths(12);
        long inicioEpoch = inicio.toEpochSecond(ZoneOffset.UTC);
        long finEpoch = ahora.minusMinutes(1).toEpochSecond(ZoneOffset.UTC);
        long aleatorio = inicioEpoch + (long) (random.nextDouble() * Math.max(1L, finEpoch - inicioEpoch));
        return LocalDateTime.ofEpochSecond(aleatorio, 0, ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES);
    }

    private LocalDateTime calcularLlegada(LocalDateTime salida, BigDecimal duracionHoras) {
        long minutos = duracionHoras.multiply(BigDecimal.valueOf(60)).setScale(0, RoundingMode.HALF_UP).longValue();
        return salida.plusMinutes(minutos);
    }

    private LocalDateTime generarFechaReservaCoherente(LocalDateTime salidaViaje) {
        int diasAntes = 1 + random.nextInt(60);
        int horasAntes = random.nextInt(12);
        return salidaViaje.minusDays(diasAntes).minusHours(horasAntes).truncatedTo(ChronoUnit.MINUTES);
    }

    private int generarAsientoDisponible(int capacidadTotal, Set<Integer> asientosUsados) {
        while (true) {
            int asiento = 1 + random.nextInt(capacidadTotal);
            if (asientosUsados.add(asiento)) {
                return asiento;
            }
        }
    }

    private PackageCatalog seleccionarPaqueteAleatorio(List<PackageCatalog> paquetes, int reservaId) {
        if (paquetes.isEmpty()) {
            return null;
        }
        if (reservaId % 100 >= 35) {
            return null;
        }
        return paquetes.get(random.nextInt(paquetes.size()));
    }

    private BigDecimal calcularMontoReserva(BigDecimal precioBase, PackageCatalog paquete, int cantidadPasajeros) {
        BigDecimal montoBase = precioBase.multiply(BigDecimal.valueOf(cantidadPasajeros));
        if (paquete == null) {
            return montoBase.setScale(2, RoundingMode.HALF_UP);
        }
        return montoBase.add(paquete.precioTotal().multiply(BigDecimal.valueOf(0.25))).setScale(2, RoundingMode.HALF_UP);
    }

    private String generarCuponSiCorresponde(int reservaId, boolean esCompletada, PackageCatalog paquete) {
        if (paquete == null) {
            return null;
        }
        if (reservaId % 100 >= 35) {
            return null;
        }
        String prefijo = esCompletada ? "PROMO" : "RECUPERA";
        return prefijo + "-" + paquete.id() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
    }

    private record RoleCatalog(long id, String nombre) {
    }

    private record RouteCatalog(long id, BigDecimal duracionHoras, BigDecimal precioBase) {
    }

    private record FleetCatalog(long id, int capacidadTotalAsientos) {
    }

    private record PackageCatalog(long id, BigDecimal precioTotal) {
    }

    private record TripSeed(
            long id,
            long idRuta,
            long idBus,
            LocalDateTime fechaSalida,
            LocalDateTime fechaLlegada,
            BigDecimal precioBase,
            int capacidadTotalAsientos) {
    }
}
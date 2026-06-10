package com.agencia.viajes.transaccional.gerencial.service;

import com.agencia.viajes.transaccional.gerencial.dto.DistribucionClustersResponse;
import com.agencia.viajes.transaccional.gerencial.dto.DistribucionClustersResponse.Centroide;
import com.agencia.viajes.transaccional.gerencial.dto.DistribucionClustersResponse.ClusterDetalle;
import com.agencia.viajes.transaccional.gerencial.dto.DistribucionClustersResponse.Metricas;
import com.agencia.viajes.transaccional.gerencial.dto.EvolucionClustersResponse;
import com.agencia.viajes.transaccional.gerencial.dto.EvolucionClustersResponse.ClusterCantidad;
import com.agencia.viajes.transaccional.gerencial.dto.EvolucionClustersResponse.PuntoSerie;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse.ClusterResumen;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse.ConversionCluster;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse.ReglasAsociacionResumen;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse.SegmentacionResumen;
import com.agencia.viajes.transaccional.gerencial.dto.MapaRutasComplementariasResponse;
import com.agencia.viajes.transaccional.gerencial.dto.MapaRutasComplementariasResponse.CoOcurrencia;
import com.agencia.viajes.transaccional.gerencial.dto.ReglaAsociacionEnriquecida;
import com.agencia.viajes.transaccional.gerencial.dto.ReglaAsociacionEnriquecida.RutaBasica;
import com.agencia.viajes.transaccional.gerencial.dto.RutasPorClusterResponse;
import com.agencia.viajes.transaccional.gerencial.dto.RutasPorClusterResponse.RutaFrecuente;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU10ReglasResponse;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU11SegmentarResponse;
import com.agencia.viajes.transaccional.gerencial.model.DashboardKpiSnapshot;
import com.agencia.viajes.transaccional.gerencial.model.ReglaAsociacionCache;
import com.agencia.viajes.transaccional.gerencial.model.UsuarioClusterHistorico;
import com.agencia.viajes.transaccional.gerencial.repository.DashboardKpiSnapshotRepository;
import com.agencia.viajes.transaccional.gerencial.repository.ReglaAsociacionCacheRepository;
import com.agencia.viajes.transaccional.gerencial.repository.UsuarioClusterHistoricoRepository;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.rutas.repository.RutaDestinoRepository;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Service
@RequiredArgsConstructor
public class GerencialService {

    private static final Logger log = LoggerFactory.getLogger(GerencialService.class);

    @Value("${bi.segmentacion.batch-size:50}")
    private int segmentacionBatchSize;

    @Value("${bi.segmentacion.parallelism:3}")
    private int segmentacionParallelism;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("[BI-INIT] Forzando carga de reglas y resegmentación para llenar dashboard...");
        dashboardKpiSnapshotRepository.deleteAll();
        reglaAsociacionCacheRepository.deleteAll();
        usuarioClusterHistoricoRepository.deleteAll();

        refrescarReglasDeAsociacion();
        ejecutarResegmentacionMasiva();
    }

    private final MotorIaGerencialClient motorIaClient;
    private final ClusterCalculadorService clusterCalculadorService;
    private final UsuarioClusterHistoricoRepository usuarioClusterHistoricoRepository;
    private final ReglaAsociacionCacheRepository reglaAsociacionCacheRepository;
    private final DashboardKpiSnapshotRepository dashboardKpiSnapshotRepository;
    private final RutaDestinoRepository rutaDestinoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    // --- Endpoints Lectura Dashboard ---

    @Transactional(readOnly = true)
    public KpisGeneralesResponse obtenerKpisGenerales() {
        Optional<DashboardKpiSnapshot> snapshotOpt = dashboardKpiSnapshotRepository.findTopByOrderByFechaSnapshotDesc();
        if (snapshotOpt.isEmpty()) {
            return KpisGeneralesResponse.builder().build(); // Vacío si no hay datos
        }

        DashboardKpiSnapshot snapshot = snapshotOpt.get();
        Map<Integer, String> etiquetas = clusterCalculadorService.inferirEtiquetasClusters();

        try {
            Map<String, Integer> distribucion = objectMapper.readValue(snapshot.getDistribucionClusters(),
                    new TypeReference<>() {
                    });
            Map<String, Double> ingresos = objectMapper.readValue(snapshot.getIngresoPorCluster(),
                    new TypeReference<>() {
                    });
            Map<String, Double> conversion = objectMapper.readValue(snapshot.getConversionPorCluster(),
                    new TypeReference<>() {
                    });

            List<ClusterResumen> clusters = distribucion.entrySet().stream().map(e -> {
                int clusterId = Integer.parseInt(e.getKey());
                int cantidad = e.getValue();
                double pct = snapshot.getTotalUsuariosSegmentados() > 0
                        ? (cantidad * 100.0) / snapshot.getTotalUsuariosSegmentados()
                        : 0.0;
                double ingresoTotal = ingresos.getOrDefault(e.getKey(), 0.0);
                double ingresoProm = cantidad > 0 ? ingresoTotal / cantidad : 0.0;

                return ClusterResumen.builder()
                        .clusterId(clusterId)
                        .etiqueta(etiquetas.getOrDefault(clusterId, "Cluster " + clusterId))
                        .cantidad(cantidad)
                        .porcentaje(Math.round(pct * 10.0) / 10.0)
                        .ingresoPromedio(Math.round(ingresoProm * 100.0) / 100.0)
                        .build();
            }).toList();

            List<ConversionCluster> conversionPorCluster = conversion.entrySet().stream()
                    .map(e -> ConversionCluster.builder()
                            .clusterId(Integer.parseInt(e.getKey()))
                            .tasaConversion(Math.round(e.getValue() * 100.0) / 100.0)
                            .build())
                    .toList();

            return KpisGeneralesResponse.builder()
                    .fechaSnapshot(snapshot.getFechaSnapshot())
                    .segmentacion(SegmentacionResumen.builder()
                            .totalUsuarios(snapshot.getTotalUsuarios())
                            .totalSegmentados(snapshot.getTotalUsuariosSegmentados())
                            .clusters(clusters)
                            .conversionPorCluster(conversionPorCluster)
                            .build())
                    .reglasAsociacion(ReglasAsociacionResumen.builder()
                            .totalReglas(snapshot.getTotalReglasAsociacion())
                            .reglasAltoLift(snapshot.getReglasAltoLiftCount())
                            .supportPromedioTop20(snapshot.getSupportPromedioTop20() != null
                                    ? snapshot.getSupportPromedioTop20().doubleValue()
                                    : 0.0)
                            .indiceCrossSelling(snapshot.getIndiceCrossSelling())
                            .build())
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Error al parsear JSON del snapshot", e);
            throw new RuntimeException("Error interno al leer KPIs", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ReglaAsociacionEnriquecida> obtenerReglasAsociacion(int top, String ordenarPor) {
        List<ReglaAsociacionCache> reglas = switch (ordenarPor.toLowerCase()) {
            case "confidence" -> reglaAsociacionCacheRepository.findAllByOrderByConfianzaDesc();
            case "support" -> reglaAsociacionCacheRepository.findAllByOrderBySoporteDesc();
            default -> reglaAsociacionCacheRepository.findAllByOrderByLiftDesc();
        };
        if (reglas.size() > top) {
            reglas = reglas.subList(0, top);
        }

        List<RutaDestino> todasLasRutas = rutaDestinoRepository.findAll();
        Map<Integer, String> rutasMap = todasLasRutas.stream()
                .collect(Collectors.toMap(RutaDestino::getId, r -> r.getCiudadOrigen() + " → " + r.getCiudadDestino()));

        return reglas.stream().map(r -> {
            try {
                List<Integer> ants = objectMapper.readValue(r.getAntecedents(), new TypeReference<>() {
                });
                List<RutaBasica> antecedentes = ants.stream()
                        .map(id -> RutaBasica.builder().idRuta(id).descripcion(rutasMap.getOrDefault(id, "Desconocida"))
                                .build())
                        .toList();

                // El backend de Python a veces manda un array o un int para el consecuente,
                // asumo int único por ahora basado en CU10ReglasResponse
                Integer consId = Integer.parseInt(r.getConsequents());
                RutaBasica consecuente = RutaBasica.builder()
                        .idRuta(consId)
                        .descripcion(rutasMap.getOrDefault(consId, "Desconocida"))
                        .build();

                String antStr = antecedentes.stream().map(RutaBasica::getDescripcion).collect(Collectors.joining(", "));
                String intp = String.format("El %.0f%% de quienes compran %s también compran %s",
                        r.getConfianza().doubleValue() * 100, antStr, consecuente.getDescripcion());

                return ReglaAsociacionEnriquecida.builder()
                        .antecedentes(antecedentes)
                        .consecuente(consecuente)
                        .soporte(r.getSoporte())
                        .confianza(r.getConfianza())
                        .lift(r.getLift())
                        .interpretacion(intp)
                        .build();

            } catch (Exception e) {
                log.error("Error procesando regla de la cache", e);
                return null;
            }
        }).filter(r -> r != null).toList();
    }

    @Transactional(readOnly = true)
    public DistribucionClustersResponse obtenerDistribucionClusters() {
        // En una app real esto leería de una vista materializada o haría una
        // agregación.
        // Por simplicidad, usamos una query nativa sobre el último snapshot de cada
        // usuario.
        String sql = """
                    WITH UltimoCluster AS (
                        SELECT id_usuario, cluster_id, total_gastado, num_reservas, rutas_distintas, promedio_pasajeros,
                               ROW_NUMBER() OVER(PARTITION BY id_usuario ORDER BY fecha_asignacion DESC) as rn
                        FROM USUARIO_CLUSTER_HISTORICO
                    )
                    SELECT cluster_id,
                           COUNT(id_usuario) as cantidad,
                           AVG(total_gastado) as avg_gasto,
                           AVG(num_reservas) as avg_reservas,
                           AVG(rutas_distintas) as avg_rutas,
                           AVG(promedio_pasajeros) as avg_pasajeros,
                           SUM(total_gastado) as total_gasto
                    FROM UltimoCluster
                    WHERE rn = 1
                    GROUP BY cluster_id
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<Integer, String> etiquetas = clusterCalculadorService.inferirEtiquetasClusters();

        int totalUsuarios = rows.stream().mapToInt(r -> ((Number) r.get("cantidad")).intValue()).sum();

        List<ClusterDetalle> clusters = rows.stream().map(row -> {
            int clusterId = ((Number) row.get("cluster_id")).intValue();
            int cantidad = ((Number) row.get("cantidad")).intValue();
            double pct = totalUsuarios > 0 ? (cantidad * 100.0) / totalUsuarios : 0;

            return ClusterDetalle.builder()
                    .clusterId(clusterId)
                    .etiqueta(etiquetas.getOrDefault(clusterId, "Cluster " + clusterId))
                    .cantidadUsuarios(cantidad)
                    .porcentaje(Math.round(pct * 10.0) / 10.0)
                    .centroide(Centroide.builder()
                            .totalGastado(((Number) row.get("avg_gasto")).doubleValue())
                            .numReservas(((Number) row.get("avg_reservas")).intValue())
                            .rutasDistintas(((Number) row.get("avg_rutas")).intValue())
                            .promedioPasajeros(((Number) row.get("avg_pasajeros")).doubleValue())
                            .build())
                    .metricas(Metricas.builder()
                            .ingresoTotal(((Number) row.get("total_gasto")).doubleValue())
                            .ingresoPromedio(
                                    cantidad > 0 ? ((Number) row.get("total_gasto")).doubleValue() / cantidad : 0)
                            // Conversión y ticket son mockeados aquí para simplicidad del MVP
                            .tasaConversion(0.7)
                            .ticketPromedio(150.0)
                            .build())
                    .build();
        }).toList();

        return DistribucionClustersResponse.builder()
                .fechaUltimaSegmentacion(LocalDateTime.now())
                .nClusters(clusters.size())
                .clusters(clusters)
                .build();
    }

    @Transactional(readOnly = true)
    public EvolucionClustersResponse obtenerEvolucionClusters(String fechaInicio, String fechaFin, String intervalo) {
        // En el MVP usamos query agrupada mensual.
        String sql = """
                    WITH Meses AS (
                        SELECT date_trunc('month', fecha_asignacion) as mes, cluster_id, COUNT(id_usuario) as cantidad
                        FROM USUARIO_CLUSTER_HISTORICO
                        WHERE fecha_asignacion >= ?::timestamp AND fecha_asignacion <= ?::timestamp
                        GROUP BY mes, cluster_id
                    )
                    SELECT to_char(mes, 'YYYY-MM') as fecha_str, cluster_id, cantidad
                    FROM Meses
                    ORDER BY mes ASC, cluster_id ASC
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, fechaInicio + " 00:00:00",
                fechaFin + " 23:59:59");

        Map<String, List<ClusterCantidad>> seriesMap = new HashMap<>();
        Map<String, Integer> totalesPorMes = new HashMap<>();

        for (Map<String, Object> row : rows) {
            String fecha = (String) row.get("fecha_str");
            int clusterId = ((Number) row.get("cluster_id")).intValue();
            int cantidad = ((Number) row.get("cantidad")).intValue();

            totalesPorMes.put(fecha, totalesPorMes.getOrDefault(fecha, 0) + cantidad);

            seriesMap.putIfAbsent(fecha, new ArrayList<>());
            seriesMap.get(fecha).add(ClusterCantidad.builder().clusterId(clusterId).cantidad(cantidad).build());
        }

        List<PuntoSerie> serie = seriesMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    String fecha = e.getKey();
                    int totalMes = totalesPorMes.get(fecha);
                    List<ClusterCantidad> cl = e.getValue().stream().map(c -> {
                        c.setPorcentaje(
                                totalMes > 0 ? Math.round((c.getCantidad() * 100.0) / totalMes * 10.0) / 10.0 : 0.0);
                        return c;
                    }).toList();
                    return PuntoSerie.builder().fecha(fecha).clusters(cl).build();
                })
                .toList();

        Map<String, Integer> migracionesMock = new HashMap<>();
        migracionesMock.put("economicoAModerado", 12);
        migracionesMock.put("moderadoAPremium", 8);

        return EvolucionClustersResponse.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .intervalo(intervalo)
                .serie(serie)
                .migraciones(migracionesMock)
                .build();
    }

    @Transactional(readOnly = true)
    public MapaRutasComplementariasResponse obtenerMapaRutasComplementarias() {
        List<ReglaAsociacionEnriquecida> topReglas = obtenerReglasAsociacion(100, "lift");

        Map<Integer, RutaBasica> rutasUnicas = new HashMap<>();
        List<CoOcurrencia> matriz = new ArrayList<>();

        for (ReglaAsociacionEnriquecida r : topReglas) {
            rutasUnicas.put(r.getConsecuente().getIdRuta(), r.getConsecuente());
            for (RutaBasica ant : r.getAntecedentes()) {
                rutasUnicas.put(ant.getIdRuta(), ant);
                matriz.add(CoOcurrencia.builder()
                        .rutaOrigen(ant.getIdRuta())
                        .rutaDestino(r.getConsecuente().getIdRuta())
                        .lift(r.getLift())
                        .confianza(r.getConfianza())
                        .build());
            }
        }

        return MapaRutasComplementariasResponse.builder()
                .rutas(new ArrayList<>(rutasUnicas.values()))
                .matriz(matriz)
                .build();
    }

    @Transactional(readOnly = true)
    public RutasPorClusterResponse obtenerRutasPorCluster(int clusterId) {
        String sql = """
                    WITH UltimoCluster AS (
                        SELECT id_usuario, cluster_id,
                               ROW_NUMBER() OVER(PARTITION BY id_usuario ORDER BY fecha_asignacion DESC) as rn
                        FROM USUARIO_CLUSTER_HISTORICO
                    )
                    SELECT v.id_ruta, rd.ciudad_origen, rd.ciudad_destino, COUNT(r.id_reserva) as frec, SUM(r.monto_total_pagado) as ingreso
                    FROM RESERVA r
                    JOIN VIAJE_PROGRAMADO v ON r.id_viaje = v.id_viaje
                    JOIN RUTA_DESTINO rd ON v.id_ruta = rd.id_ruta
                    JOIN UltimoCluster uc ON r.id_usuario = uc.id_usuario
                    WHERE uc.rn = 1 AND uc.cluster_id = ? AND r.estado_reserva <> 'CANCELADA'
                    GROUP BY v.id_ruta, rd.ciudad_origen, rd.ciudad_destino
                    ORDER BY frec DESC
                    LIMIT 5
                """;

        List<RutaFrecuente> frecuentes = jdbcTemplate.query(sql, (rs, rowNum) -> RutaFrecuente.builder()
                .idRuta(rs.getInt("id_ruta"))
                .descripcion(rs.getString("ciudad_origen") + " → " + rs.getString("ciudad_destino"))
                .frecuencia(rs.getLong("frec"))
                .ingresoTotal(rs.getBigDecimal("ingreso"))
                .build(), clusterId);

        Map<Integer, String> etiquetas = clusterCalculadorService.inferirEtiquetasClusters();

        // Obtener reglas asociadas a estas rutas principales
        List<ReglaAsociacionEnriquecida> todasReglas = obtenerReglasAsociacion(50, "lift");
        List<ReglaAsociacionEnriquecida> reglasRelevantes = todasReglas.stream()
                .filter(r -> r.getAntecedentes().stream()
                        .anyMatch(a -> frecuentes.stream().anyMatch(f -> f.getIdRuta().equals(a.getIdRuta()))))
                .limit(5)
                .toList();

        return RutasPorClusterResponse.builder()
                .clusterId(clusterId)
                .etiqueta(etiquetas.getOrDefault(clusterId, "Cluster " + clusterId))
                .rutasFrecuentes(frecuentes)
                .reglasRelevantes(reglasRelevantes)
                .build();
    }

    // --- Endpoints de Actualización Asíncrona (Escritura) ---

    @Transactional
    public void refrescarReglasDeAsociacion() {
        log.info("[BI] Iniciando actualización de reglas de asociación (CU10)");
        CU10ReglasResponse response = motorIaClient.obtenerReglasAsociacion();
        if (response != null && response.getReglas() != null && !response.getReglas().isEmpty()) {
            reglaAsociacionCacheRepository.deleteAllInBatch(); // Simula TRUNCATE
            List<ReglaAsociacionCache> entities = response.getReglas().stream().map(r -> {
                ReglaAsociacionCache entity = new ReglaAsociacionCache();
                try {
                    entity.setAntecedents(objectMapper.writeValueAsString(r.getAntecedents()));
                    entity.setConsequents(String.valueOf(r.getConsequents()));
                    entity.setSoporte(r.getSupport());
                    entity.setConfianza(r.getConfidence());
                    entity.setLift(r.getLift());
                    entity.setFechaCarga(LocalDateTime.now());
                } catch (JsonProcessingException e) {
                    log.error("Error serializando reglas", e);
                }
                return entity;
            }).toList();

            reglaAsociacionCacheRepository.saveAll(entities);
            log.info("[BI] Actualizadas {} reglas de asociación.", entities.size());
            actualizarSnapshot();
        } else {
            log.warn("[BI] No se pudieron cargar reglas (motor IA no respondió o devolvió lista vacía). Se retiene la caché actual.");
        }
    }

    @Transactional
    public void ejecutarResegmentacionMasiva() {
        log.info("[BI] Iniciando resegmentación masiva (CU11) con batching (batch={}, paralelismo={})",
                segmentacionBatchSize, segmentacionParallelism);
        org.springframework.data.domain.Page<Usuario> pagina = usuarioRepository
                .findAll(org.springframework.data.domain.PageRequest.of(0, segmentacionBatchSize));
        int procesados = 0;

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                .newFixedThreadPool(segmentacionParallelism);

        try {
            while (!pagina.isEmpty()) {
                List<java.util.concurrent.CompletableFuture<Void>> futures = pagina.getContent().stream()
                        .map(u -> java.util.concurrent.CompletableFuture
                                .runAsync(() -> actualizarClusterDeUsuario(u.getId()), executor))
                        .toList();

                java.util.concurrent.CompletableFuture
                        .allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
                procesados += pagina.getNumberOfElements();
                log.info("[BI] Procesados {} usuarios para segmentación...", procesados);

                if (pagina.hasNext()) {
                    pagina = usuarioRepository.findAll(pagina.nextPageable());
                } else {
                    break;
                }
            }
        } finally {
            executor.shutdown();
        }

        log.info("[BI] Segmentación completada para {} usuarios.", procesados);
        actualizarSnapshot();
    }

    @Transactional
    public void actualizarClusterDeUsuario(Integer idUsuario) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Map<String, Object> features = clusterCalculadorService.calcularCaracteristicasUsuario(idUsuario);
            // Solo segmentar si tiene al menos algo de gasto o reservas
            if (((BigDecimal) features.get("total_gastado")).compareTo(BigDecimal.ZERO) > 0) {
                CU11SegmentarResponse response = motorIaClient.segmentarUsuario(features);
                if (response != null && response.getCluster() != null) {
                    UsuarioClusterHistorico hist = new UsuarioClusterHistorico();
                    hist.setUsuario(usuarioRepository.getReferenceById(idUsuario));
                    hist.setClusterId(response.getCluster());
                    hist.setTotalGastado((BigDecimal) features.get("total_gastado"));
                    hist.setNumReservas((Integer) features.get("num_reservas"));
                    hist.setRutasDistintas((Integer) features.get("rutas_distintas"));
                    hist.setPromedioPasajeros((BigDecimal) features.get("promedio_pasajeros"));
                    hist.setFechaAsignacion(LocalDateTime.now());

                    usuarioClusterHistoricoRepository.save(hist);
                    meterRegistry.counter("bi.segmentacion.exito").increment();
                } else {
                    meterRegistry.counter("bi.segmentacion.fallo.ia").increment();
                }
            }
        } catch (Exception e) {
            log.error("[BI] Error actualizando cluster para usuario {}: {}", idUsuario, e.getMessage());
            meterRegistry.counter("bi.segmentacion.error").increment();
        } finally {
            sample.stop(meterRegistry.timer("bi.segmentacion.latencia"));
        }
    }

    private void actualizarSnapshot() {
        // Implementación simplificada del snapshot creation logic
        LocalDate hoy = LocalDate.now();

        // Comprobar si ya existe uno hoy, si es así lo actualizamos, si no lo creamos.
        DashboardKpiSnapshot snapshot = dashboardKpiSnapshotRepository.findByFechaSnapshot(hoy)
                .orElseGet(() -> {
                    DashboardKpiSnapshot s = new DashboardKpiSnapshot();
                    s.setFechaSnapshot(hoy);
                    s.setFechaCreacion(LocalDateTime.now());
                    return s;
                });

        long totalUsuarios = usuarioRepository.count();
        long segmentados = jdbcTemplate
                .queryForObject("SELECT COUNT(DISTINCT id_usuario) FROM USUARIO_CLUSTER_HISTORICO", Long.class);

        snapshot.setTotalUsuarios((int) totalUsuarios);
        snapshot.setTotalUsuariosSegmentados((int) segmentados);

        // Lógica mockeada para los JSONB para acelerar MVP
        snapshot.setDistribucionClusters("{\"0\": 10, \"1\": 20, \"2\": 5}");
        snapshot.setIngresoPorCluster("{\"0\": 500.0, \"1\": 2000.0, \"2\": 5000.0}");
        snapshot.setConversionPorCluster("{\"0\": 0.5, \"1\": 0.6, \"2\": 0.8}");

        List<ReglaAsociacionCache> reglas = reglaAsociacionCacheRepository.findAllByOrderByLiftDesc();
        snapshot.setTotalReglasAsociacion(reglas.size());

        long reglasAltoLift = reglas.stream().filter(r -> r.getLift().compareTo(new BigDecimal("1.2")) > 0).count();
        snapshot.setReglasAltoLiftCount((int) reglasAltoLift);

        double avgSupport = reglas.stream().limit(20).mapToDouble(r -> r.getSoporte().doubleValue()).average()
                .orElse(0.0);
        snapshot.setSupportPromedioTop20(new BigDecimal(avgSupport).setScale(6, RoundingMode.HALF_UP));

        long indiceCross = reglas.stream().filter(r -> r.getConfianza().compareTo(new BigDecimal("0.5")) > 0
                && r.getLift().compareTo(new BigDecimal("1.2")) > 0).count();
        snapshot.setIndiceCrossSelling((int) indiceCross);

        dashboardKpiSnapshotRepository.save(snapshot);
    }
}

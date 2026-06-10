package com.agencia.viajes.transaccional.gerencial.service;

import com.agencia.viajes.transaccional.gerencial.config.MotorIaGerencialProperties;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU10ReglasResponse;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU11EstadisticasResponse;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU11SegmentarResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MotorIaGerencialClient {

    private static final Logger log = LoggerFactory.getLogger(MotorIaGerencialClient.class);

    private final RestClient motorIaRestClient;
    private final ObjectMapper objectMapper;
    private final MotorIaGerencialProperties properties;

    @CircuitBreaker(name = "motorIaReglas", fallbackMethod = "obtenerReglasFallback")
    @Retry(name = "motorIaReglas")
    public CU10ReglasResponse obtenerReglasAsociacion() {
        return motorIaRestClient.get()
                .uri(properties.getReglasAsociacionPath())
                .retrieve()
                .body(CU10ReglasResponse.class);
    }

    public CU10ReglasResponse obtenerReglasFallback(Exception e) {
        log.error("[CircuitBreaker] Motor IA no disponible para CU10: {}", e.getMessage());
        return null;
    }

    @CircuitBreaker(name = "motorIaStats", fallbackMethod = "obtenerEstadisticasFallback")
    @Retry(name = "motorIaStats")
    public CU11EstadisticasResponse obtenerEstadisticasClusters() {
        return motorIaRestClient.get()
                .uri(properties.getEstadisticasClustersPath())
                .retrieve()
                .body(CU11EstadisticasResponse.class);
    }

    public CU11EstadisticasResponse obtenerEstadisticasFallback(Exception e) {
        log.error("[CircuitBreaker] Motor IA no disponible para CU11 stats: {}", e.getMessage());
        return null;
    }

    @CircuitBreaker(name = "motorIaSegmentar", fallbackMethod = "segmentarUsuarioFallback")
    @Retry(name = "motorIaSegmentar")
    public CU11SegmentarResponse segmentarUsuario(Map<String, Object> caracteristicas) {
        try {
            String jsonBody = objectMapper.writeValueAsString(caracteristicas);
            return motorIaRestClient.post()
                    .uri(properties.getSegmentarUsuarioPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(CU11SegmentarResponse.class);
        } catch (JsonProcessingException e) {
            log.error("[MotorIA] Error serializando características para segmentación: {}", e.getMessage());
            return null;
        }
    }

    public CU11SegmentarResponse segmentarUsuarioFallback(Map<String, Object> caracteristicas, Exception e) {
        log.error("[CircuitBreaker] Motor IA no disponible para CU11 segmentar: {}", e.getMessage());
        return null;
    }
}

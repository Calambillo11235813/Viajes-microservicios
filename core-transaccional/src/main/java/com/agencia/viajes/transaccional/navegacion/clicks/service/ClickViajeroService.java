package com.agencia.viajes.transaccional.navegacion.clicks.service;

import com.agencia.viajes.transaccional.navegacion.clicks.dto.ClickViajeroRequest;
import com.agencia.viajes.transaccional.navegacion.clicks.model.ClickViajeroItem;
import com.agencia.viajes.transaccional.navegacion.clicks.repository.ClickViajeroRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio write-only de clicks/visualizaciones hacia {@code ClicksViajero}.
 * Los fallos de persistencia se registran pero no interrumpen el flujo principal.
 */
@Service
@RequiredArgsConstructor
public class ClickViajeroService {

    private static final Logger log = LoggerFactory.getLogger(ClickViajeroService.class);
    private static final String TIPO_VISUALIZACION_RUTA = "VISUALIZACION_RUTA";
    private static final String CANAL_DEFAULT = "APP_MOVIL";

    private final ClickViajeroRepository clickViajeroRepository;

    @Value("${aws.dynamodb.enabled:true}")
    private boolean dynamoDbEnabled;

    /**
     * Registra un click o visualización de ruta.
     *
     * @param request datos del evento.
     */
    public void registrarClick(ClickViajeroRequest request) {
        if (request == null || request.idUsuario() == null) {
            log.debug("Se omite registro de click: idUsuario es nulo.");
            return;
        }

        Instant ahora = Instant.now();
        String fechaIso = DateTimeFormatter.ISO_INSTANT.format(ahora.atOffset(ZoneOffset.UTC));

        ClickViajeroItem item = new ClickViajeroItem();
        item.setIdUsuario(request.idUsuario());
        item.setTimestamp(fechaIso + "#" + UUID.randomUUID());
        item.setFecha(fechaIso);
        item.setTipoEvento(
                request.tipoEvento() != null && !request.tipoEvento().isBlank()
                        ? request.tipoEvento().trim()
                        : TIPO_VISUALIZACION_RUTA);
        item.setIdRuta(request.idRuta());
        item.setTiempoPermanenciaSeg(request.tiempoPermanenciaSeg());
        item.setCanal(request.canal() != null && !request.canal().isBlank()
                ? request.canal().trim()
                : CANAL_DEFAULT);
        item.setDispositivo(request.dispositivo());

        persistirSilenciosamente(item);
    }

    /**
     * Compatibilidad con la mutación GraphQL existente {@code registrarVisualizacionRuta}.
     */
    public void registrarVisualizacion(
            Integer idUsuario,
            Integer idRuta,
            Integer idRutaVista,
            String canal,
            Integer tiempoPermanenciaSeg,
            String dispositivo) {
        Integer ruta = idRutaVista != null ? idRutaVista : idRuta;
        registrarClick(new ClickViajeroRequest(
                idUsuario,
                TIPO_VISUALIZACION_RUTA,
                ruta,
                tiempoPermanenciaSeg,
                canal,
                dispositivo));
    }

    private void persistirSilenciosamente(ClickViajeroItem item) {
        if (!dynamoDbEnabled) {
            log.debug(
                    "Tracking DynamoDB deshabilitado; se omite click (tipo={}, usuario={})",
                    item.getTipoEvento(),
                    item.getIdUsuario());
            return;
        }

        try {
            clickViajeroRepository.guardar(item);
        } catch (Exception e) {
            log.warn(
                    "No se pudo registrar click en DynamoDB (tipo={}, usuario={}): {}",
                    item.getTipoEvento(),
                    item.getIdUsuario(),
                    e.getMessage());
        }
    }
}

package com.agencia.viajes.transaccional.navegacion.service;

import com.agencia.viajes.transaccional.navegacion.dto.VisualizacionRutaRequest;
import com.agencia.viajes.transaccional.navegacion.model.NavegacionItem;
import com.agencia.viajes.transaccional.navegacion.repository.NavegacionRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio de tracking de navegación write-only hacia DynamoDB.
 * Los fallos de persistencia se registran pero no interrumpen el flujo principal.
 */
@Service
@RequiredArgsConstructor
public class NavegacionService {

    private static final Logger log = LoggerFactory.getLogger(NavegacionService.class);
    private static final String TIPO_VISUALIZACION_RUTA = "VISUALIZACION_RUTA";
    private static final String TIPO_BUSQUEDA_RUTA = "BUSQUEDA_RUTA";
    private static final String CANAL_DEFAULT = "GRAPHQL";

    private final NavegacionRepository navegacionRepository;

    @Value("${aws.dynamodb.enabled:true}")
    private boolean dynamoDbEnabled;

    /**
     * Registra la visualización de una ruta por parte de un usuario.
     *
     * @param request datos de la visualización.
     */
    public void registrarVisualizacion(VisualizacionRutaRequest request) {
        if (request == null || request.idUsuario() == null) {
            log.debug("Se omite registro de visualización: idUsuario es nulo.");
            return;
        }

        NavegacionItem item = new NavegacionItem();
        item.setIdUsuario(request.idUsuario());
        item.setTimestamp(generarTimestampUnico());
        item.setIdInteraccion(UUID.randomUUID().toString());
        item.setTipoEvento(TIPO_VISUALIZACION_RUTA);
        item.setTipoAccion(TIPO_VISUALIZACION_RUTA);
        item.setIdRuta(request.idRuta());
        item.setIdRutaVista(request.idRutaVista() != null ? request.idRutaVista() : request.idRuta());
        item.setOrigen(request.origen());
        item.setDestino(request.destino());
        item.setCiudadOrigenVista(
                request.ciudadOrigenVista() != null ? request.ciudadOrigenVista() : request.origen());
        item.setCiudadDestinoVista(
                request.ciudadDestinoVista() != null ? request.ciudadDestinoVista() : request.destino());
        item.setCategoriaVista(request.categoriaVista());
        item.setTiempoPermanenciaSeg(request.tiempoPermanenciaSeg());
        item.setDispositivo(request.dispositivo());
        item.setCanal(request.canal() != null && !request.canal().isBlank()
                ? request.canal().trim()
                : CANAL_DEFAULT);

        persistirSilenciosamente(item);
    }

    /**
     * Registra una búsqueda de rutas y horarios (CU-01).
     *
     * @param idUsuario identificador del usuario (opcional).
     * @param origen ciudad de origen.
     * @param destino ciudad de destino.
     * @param fecha fecha de búsqueda.
     * @param resultadosCount cantidad de resultados devueltos.
     */
    public void registrarBusquedaRuta(
            Integer idUsuario,
            String origen,
            String destino,
            LocalDate fecha,
            int resultadosCount) {
        if (idUsuario == null) {
            log.debug("Se omite registro de búsqueda: idUsuario es nulo.");
            return;
        }

        NavegacionItem item = new NavegacionItem();
        item.setIdUsuario(idUsuario);
        item.setTimestamp(generarTimestampUnico());
        item.setIdInteraccion(UUID.randomUUID().toString());
        item.setTipoEvento(TIPO_BUSQUEDA_RUTA);
        item.setTipoAccion(TIPO_BUSQUEDA_RUTA);
        item.setOrigen(origen);
        item.setDestino(destino);
        item.setFechaBusqueda(fecha != null ? fecha.toString() : null);
        item.setResultadosCount(resultadosCount);
        item.setCanal(CANAL_DEFAULT);

        persistirSilenciosamente(item);
    }

    /**
     * Variante asíncrona para no afectar el tiempo de respuesta de CU-01.
     *
     * @param idUsuario identificador del usuario (opcional).
     * @param origen ciudad de origen.
     * @param destino ciudad de destino.
     * @param fecha fecha de búsqueda.
     * @param resultadosCount cantidad de resultados devueltos.
     */
    @Async
    public void registrarBusquedaRutaAsync(
            Integer idUsuario,
            String origen,
            String destino,
            LocalDate fecha,
            int resultadosCount) {
        registrarBusquedaRuta(idUsuario, origen, destino, fecha, resultadosCount);
    }

    private void persistirSilenciosamente(NavegacionItem item) {
        if (!dynamoDbEnabled) {
            log.debug(
                    "Tracking DynamoDB deshabilitado; se omite escritura (tipo={}, usuario={})",
                    item.getTipoEvento(),
                    item.getIdUsuario());
            return;
        }

        try {
            navegacionRepository.guardar(item);
        } catch (Exception e) {
            log.warn(
                    "No se pudo registrar evento de navegación en DynamoDB (tipo={}, usuario={}): {}",
                    item.getTipoEvento(),
                    item.getIdUsuario(),
                    e.getMessage());
        }
    }

    private String generarTimestampUnico() {
        String iso = DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC));
        return iso + "#" + UUID.randomUUID();
    }
}

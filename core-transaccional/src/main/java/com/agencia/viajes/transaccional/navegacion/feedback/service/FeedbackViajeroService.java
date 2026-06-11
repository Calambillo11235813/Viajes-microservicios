package com.agencia.viajes.transaccional.navegacion.feedback.service;

import com.agencia.viajes.transaccional.navegacion.feedback.dto.FeedbackViajeroRequest;
import com.agencia.viajes.transaccional.navegacion.feedback.model.FeedbackViajeroItem;
import com.agencia.viajes.transaccional.navegacion.feedback.repository.FeedbackViajeroRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio write-only de feedback post-viaje hacia {@code FeedbackViajero}.
 * Los fallos de persistencia se registran pero no interrumpen el flujo principal.
 */
@Service
@RequiredArgsConstructor
public class FeedbackViajeroService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackViajeroService.class);

    private final FeedbackViajeroRepository feedbackViajeroRepository;

    @Value("${aws.dynamodb.enabled:true}")
    private boolean dynamoDbEnabled;

    /**
     * Registra comentario y calificación de un viaje completado.
     *
     * @param request datos del feedback.
     */
    public void registrarFeedback(FeedbackViajeroRequest request) {
        if (request == null || request.idUsuario() == null) {
            log.debug("Se omite registro de feedback: idUsuario es nulo.");
            return;
        }
        if (request.idViaje() == null) {
            log.debug("Se omite registro de feedback: idViaje es nulo.");
            return;
        }
        if (request.calificacion() == null || request.calificacion() < 1 || request.calificacion() > 5) {
            log.debug(
                    "Se omite registro de feedback: calificacion invalida (valor={}).",
                    request.calificacion());
            return;
        }

        String fechaIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now().atOffset(ZoneOffset.UTC));

        FeedbackViajeroItem item = new FeedbackViajeroItem();
        item.setIdUsuario(request.idUsuario());
        item.setIdViaje(request.idViaje());
        item.setFecha(fechaIso);
        item.setCalificacion(request.calificacion());
        item.setComentario(request.comentario());
        item.setIdReserva(request.idReserva());

        persistirSilenciosamente(item);
    }

    private void persistirSilenciosamente(FeedbackViajeroItem item) {
        if (!dynamoDbEnabled) {
            log.debug(
                    "Tracking DynamoDB deshabilitado; se omite feedback (usuario={}, viaje={})",
                    item.getIdUsuario(),
                    item.getIdViaje());
            return;
        }

        try {
            feedbackViajeroRepository.guardar(item);
        } catch (Exception e) {
            log.warn(
                    "No se pudo registrar feedback en DynamoDB (usuario={}, viaje={}): {}",
                    item.getIdUsuario(),
                    item.getIdViaje(),
                    e.getMessage());
        }
    }
}

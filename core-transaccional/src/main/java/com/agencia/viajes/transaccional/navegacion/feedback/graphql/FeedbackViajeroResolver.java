package com.agencia.viajes.transaccional.navegacion.feedback.graphql;

import com.agencia.viajes.transaccional.navegacion.feedback.dto.FeedbackViajeroRequest;
import com.agencia.viajes.transaccional.navegacion.feedback.service.FeedbackViajeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Mutaciones GraphQL para registro de feedback en {@code FeedbackViajero}.
 */
@Controller
@RequiredArgsConstructor
public class FeedbackViajeroResolver {

    private final FeedbackViajeroService feedbackViajeroService;

    /**
     * Registra calificación y comentario de un viaje completado.
     *
     * @return {@code true} si la petición fue aceptada para registro.
     */
    @MutationMapping
    public boolean registrarFeedbackViaje(
            @Argument Integer idUsuario,
            @Argument Integer idViaje,
            @Argument Integer idReserva,
            @Argument Integer calificacion,
            @Argument String comentario) {
        feedbackViajeroService.registrarFeedback(new FeedbackViajeroRequest(
                idUsuario, idViaje, idReserva, calificacion, comentario));
        return true;
    }
}

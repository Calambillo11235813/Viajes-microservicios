package com.agencia.viajes.transaccional.navegacion.feedback.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.agencia.viajes.transaccional.navegacion.feedback.dto.FeedbackViajeroRequest;
import com.agencia.viajes.transaccional.navegacion.feedback.model.FeedbackViajeroItem;
import com.agencia.viajes.transaccional.navegacion.feedback.repository.FeedbackViajeroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class FeedbackViajeroServiceTest {

    @Mock
    private FeedbackViajeroRepository feedbackViajeroRepository;

    @InjectMocks
    private FeedbackViajeroService feedbackViajeroService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(feedbackViajeroService, "dynamoDbEnabled", true);
    }

    @Test
    void registrarFeedback_persisteDatosEsperados() {
        FeedbackViajeroRequest request = new FeedbackViajeroRequest(
                1, 1045, 200, 4, "Excelente servicio");

        feedbackViajeroService.registrarFeedback(request);

        ArgumentCaptor<FeedbackViajeroItem> captor = ArgumentCaptor.forClass(FeedbackViajeroItem.class);
        verify(feedbackViajeroRepository, times(1)).guardar(captor.capture());

        FeedbackViajeroItem item = captor.getValue();
        assertEquals(1, item.getIdUsuario());
        assertEquals(1045, item.getIdViaje());
        assertEquals(200, item.getIdReserva());
        assertEquals(4, item.getCalificacion());
        assertEquals("Excelente servicio", item.getComentario());
        assertNotNull(item.getFecha());
    }

    @Test
    void registrarFeedback_calificacionInvalida_noPersiste() {
        feedbackViajeroService.registrarFeedback(new FeedbackViajeroRequest(1, 1045, null, 0, "Mal"));
        feedbackViajeroService.registrarFeedback(new FeedbackViajeroRequest(1, 1045, null, 6, "Mal"));

        verify(feedbackViajeroRepository, never()).guardar(any());
    }

    @Test
    void registrarFeedback_sinIdUsuario_noPersiste() {
        feedbackViajeroService.registrarFeedback(new FeedbackViajeroRequest(null, 1045, null, 4, "Ok"));

        verify(feedbackViajeroRepository, never()).guardar(any());
    }

    @Test
    void registrarFeedback_sinIdViaje_noPersiste() {
        feedbackViajeroService.registrarFeedback(new FeedbackViajeroRequest(1, null, null, 4, "Ok"));

        verify(feedbackViajeroRepository, never()).guardar(any());
    }

    @Test
    void registrarFeedback_errorDynamoDb_noPropagaExcepcion() {
        doThrow(new RuntimeException("DynamoDB no disponible"))
                .when(feedbackViajeroRepository)
                .guardar(any(FeedbackViajeroItem.class));

        assertDoesNotThrow(() -> feedbackViajeroService.registrarFeedback(
                new FeedbackViajeroRequest(1, 1045, null, 5, "Genial")));
    }

    @Test
    void registrarFeedback_dynamoDbDeshabilitado_noPersiste() {
        ReflectionTestUtils.setField(feedbackViajeroService, "dynamoDbEnabled", false);

        feedbackViajeroService.registrarFeedback(new FeedbackViajeroRequest(1, 1045, null, 5, "Genial"));

        verify(feedbackViajeroRepository, never()).guardar(any());
    }
}

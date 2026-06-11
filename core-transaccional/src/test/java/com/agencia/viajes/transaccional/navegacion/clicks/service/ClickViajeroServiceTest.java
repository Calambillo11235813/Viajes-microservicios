package com.agencia.viajes.transaccional.navegacion.clicks.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.agencia.viajes.transaccional.navegacion.clicks.dto.ClickViajeroRequest;
import com.agencia.viajes.transaccional.navegacion.clicks.model.ClickViajeroItem;
import com.agencia.viajes.transaccional.navegacion.clicks.repository.ClickViajeroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class ClickViajeroServiceTest {

    @Mock
    private ClickViajeroRepository clickViajeroRepository;

    @InjectMocks
    private ClickViajeroService clickViajeroService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(clickViajeroService, "dynamoDbEnabled", true);
    }

    @Test
    void registrarClick_persisteEventoConCamposEsperados() {
        ClickViajeroRequest request = new ClickViajeroRequest(
                1, "VISUALIZACION_RUTA", 16, 120, "APP_MOVIL", "android");

        clickViajeroService.registrarClick(request);

        ArgumentCaptor<ClickViajeroItem> captor = ArgumentCaptor.forClass(ClickViajeroItem.class);
        verify(clickViajeroRepository, times(1)).guardar(captor.capture());

        ClickViajeroItem item = captor.getValue();
        assertEquals(1, item.getIdUsuario());
        assertEquals("VISUALIZACION_RUTA", item.getTipoEvento());
        assertEquals(16, item.getIdRuta());
        assertEquals(120, item.getTiempoPermanenciaSeg());
        assertEquals("APP_MOVIL", item.getCanal());
        assertEquals("android", item.getDispositivo());
        assertNotNull(item.getTimestamp());
        assertNotNull(item.getFecha());
    }

    @Test
    void registrarVisualizacion_usaIdRutaVistaComoFallback() {
        clickViajeroService.registrarVisualizacion(1, 10, 16, "APP_MOVIL", 90, "ios");

        ArgumentCaptor<ClickViajeroItem> captor = ArgumentCaptor.forClass(ClickViajeroItem.class);
        verify(clickViajeroRepository).guardar(captor.capture());
        assertEquals(16, captor.getValue().getIdRuta());
    }

    @Test
    void registrarClick_sinIdUsuario_noPersiste() {
        clickViajeroService.registrarClick(
                new ClickViajeroRequest(null, "VISUALIZACION_RUTA", 16, 120, "APP_MOVIL", "android"));

        verify(clickViajeroRepository, never()).guardar(any());
    }

    @Test
    void registrarClick_errorDynamoDb_noPropagaExcepcion() {
        doThrow(new RuntimeException("DynamoDB no disponible"))
                .when(clickViajeroRepository)
                .guardar(any(ClickViajeroItem.class));

        assertDoesNotThrow(() -> clickViajeroService.registrarClick(new ClickViajeroRequest(
                1, "VISUALIZACION_RUTA", 16, 120, "APP_MOVIL", "android")));
    }

    @Test
    void registrarClick_dynamoDbDeshabilitado_noPersiste() {
        ReflectionTestUtils.setField(clickViajeroService, "dynamoDbEnabled", false);

        clickViajeroService.registrarClick(new ClickViajeroRequest(
                1, "VISUALIZACION_RUTA", 16, 120, "APP_MOVIL", "android"));

        verify(clickViajeroRepository, never()).guardar(any());
    }
}

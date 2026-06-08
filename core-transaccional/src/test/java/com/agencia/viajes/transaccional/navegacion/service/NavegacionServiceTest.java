package com.agencia.viajes.transaccional.navegacion.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.agencia.viajes.transaccional.navegacion.dto.VisualizacionRutaRequest;
import com.agencia.viajes.transaccional.navegacion.model.NavegacionItem;
import com.agencia.viajes.transaccional.navegacion.repository.NavegacionRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NavegacionServiceTest {

    @Mock
    private NavegacionRepository navegacionRepository;

    @InjectMocks
    private NavegacionService navegacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registrarVisualizacion_persisteEventoEnriquecido() {
        VisualizacionRutaRequest request = new VisualizacionRutaRequest(
                1,
                10,
                "La Paz",
                "Uyuni",
                "APP_MOVIL",
                "Aventura",
                "La Paz",
                "Uyuni",
                10,
                0,
                "android");

        navegacionService.registrarVisualizacion(request);

        ArgumentCaptor<NavegacionItem> captor = ArgumentCaptor.forClass(NavegacionItem.class);
        verify(navegacionRepository, times(1)).guardar(captor.capture());

        NavegacionItem item = captor.getValue();
        assertEquals(1, item.getIdUsuario());
        assertEquals("VISUALIZACION_RUTA", item.getTipoEvento());
        assertEquals("VISUALIZACION_RUTA", item.getTipoAccion());
        assertEquals(10, item.getIdRuta());
        assertEquals(10, item.getIdRutaVista());
        assertEquals("La Paz", item.getCiudadOrigenVista());
        assertEquals("Uyuni", item.getCiudadDestinoVista());
        assertEquals("Aventura", item.getCategoriaVista());
        assertEquals(0, item.getTiempoPermanenciaSeg());
        assertEquals("android", item.getDispositivo());
        assertEquals("APP_MOVIL", item.getCanal());
    }

    @Test
    void registrarVisualizacion_usaFallbacksCuandoFaltanCamposVista() {
        VisualizacionRutaRequest request = new VisualizacionRutaRequest(
                1,
                10,
                "La Paz",
                "Uyuni",
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        navegacionService.registrarVisualizacion(request);

        ArgumentCaptor<NavegacionItem> captor = ArgumentCaptor.forClass(NavegacionItem.class);
        verify(navegacionRepository, times(1)).guardar(captor.capture());

        NavegacionItem item = captor.getValue();
        assertEquals(10, item.getIdRutaVista());
        assertEquals("La Paz", item.getCiudadOrigenVista());
        assertEquals("Uyuni", item.getCiudadDestinoVista());
        assertEquals("GRAPHQL", item.getCanal());
    }

    @Test
    void registrarVisualizacion_sinIdUsuario_noPersiste() {
        VisualizacionRutaRequest request = new VisualizacionRutaRequest(
                null, 10, "La Paz", "Uyuni", "APP_MOVIL", null, null, null, null, null, null);

        navegacionService.registrarVisualizacion(request);

        verify(navegacionRepository, never()).guardar(any());
    }

    @Test
    void registrarBusquedaRuta_errorDynamoDb_noPropagaExcepcion() {
        doThrow(new RuntimeException("DynamoDB no disponible"))
                .when(navegacionRepository)
                .guardar(any(NavegacionItem.class));

        assertDoesNotThrow(() -> navegacionService.registrarBusquedaRuta(
                1, "La Paz", "Uyuni", LocalDate.of(2026, 6, 7), 3));
    }

    @Test
    void registrarBusquedaRuta_sinIdUsuario_noPersiste() {
        navegacionService.registrarBusquedaRuta(null, "La Paz", "Uyuni", LocalDate.of(2026, 6, 7), 2);

        verify(navegacionRepository, never()).guardar(any());
    }
}

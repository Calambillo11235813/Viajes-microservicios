package com.agencia.viajes.transaccional.navegacion.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.agencia.viajes.transaccional.navegacion.model.NavegacionItem;
import com.agencia.viajes.transaccional.navegacion.repository.NavegacionRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void registrarVisualizacion_persisteEvento() {
        navegacionService.registrarVisualizacion(1, 10, "La Paz", "Uyuni", "APP_MOVIL");

        verify(navegacionRepository, times(1)).guardar(any(NavegacionItem.class));
    }

    @Test
    void registrarVisualizacion_sinIdUsuario_noPersiste() {
        navegacionService.registrarVisualizacion(null, 10, "La Paz", "Uyuni", "APP_MOVIL");

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

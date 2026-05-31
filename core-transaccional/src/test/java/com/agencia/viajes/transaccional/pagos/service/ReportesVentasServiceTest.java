package com.agencia.viajes.transaccional.pagos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.agencia.viajes.transaccional.pagos.dto.ReporteVentasResponse;
import com.agencia.viajes.transaccional.pagos.repository.PagoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReportesVentasServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private ReportesVentasService reportesVentasService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerarReporteVentas_Exito() {
        // Arrange
        String fechaInicioStr = "2026-05-01";
        String fechaFinStr = "2026-05-31";

        List<Object[]> mockRows = new ArrayList<>();
        mockRows.add(new Object[] { "2026-05-15", new BigDecimal("100.00"), 2L });
        mockRows.add(new Object[] { "2026-05-16", new BigDecimal("250.00"), 3L });

        when(pagoRepository.findVentasAgrupadasPorFecha(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockRows);

        // Act
        ReporteVentasResponse response = reportesVentasService.generarReporteVentas(fechaInicioStr, fechaFinStr);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("350.00"), response.montoTotal());
        assertEquals(5, response.cantidadPagos());
        assertEquals(2, response.detallesPorFecha().size());
        assertEquals("2026-05-15", response.detallesPorFecha().get(0).fecha());
        assertEquals(new BigDecimal("100.00"), response.detallesPorFecha().get(0).montoDia());
        assertEquals(2, response.detallesPorFecha().get(0).cantidadPagosDia());

        verify(pagoRepository, times(1)).findVentasAgrupadasPorFecha(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGenerarReporteVentas_FechasInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> {
            reportesVentasService.generarReporteVentas(null, "2026-05-31");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reportesVentasService.generarReporteVentas("2026-05-01", "fecha-invalida");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            reportesVentasService.generarReporteVentas("2026-05-31", "2026-05-01");
        });
    }
}

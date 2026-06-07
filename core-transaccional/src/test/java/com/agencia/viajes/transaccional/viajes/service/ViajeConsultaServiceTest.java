package com.agencia.viajes.transaccional.viajes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.navegacion.service.NavegacionService;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ViajeConsultaServiceTest {

    @Mock
    private ViajeProgramadoRepository viajeProgramadoRepository;

    @Mock
    private TarifaViajeService tarifaViajeService;

    @Mock
    private NavegacionService navegacionService;

    @InjectMocks
    private ViajeConsultaService viajeConsultaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing()
                .when(navegacionService)
                .registrarBusquedaRutaAsync(any(), any(), any(), any(), any(Integer.class));
    }

    @Test
    void buscarRutasYHorariosDisponibles_registraNavegacionYRetornaResultados() {
        LocalDate fecha = LocalDate.of(2026, 6, 7);
        ViajeProgramado viaje = crearViajeProgramadoMock();

        when(viajeProgramadoRepository.buscarDisponiblesPorRutaYFecha(
                        eq("La Paz"), eq("Uyuni"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(viaje));
        when(tarifaViajeService.calcularPrecioPorServicio(any(BigDecimal.class), any(String.class)))
                .thenReturn(new BigDecimal("150.00"));

        var resultados = viajeConsultaService.buscarRutasYHorariosDisponibles(
                "La Paz", "Uyuni", fecha, 1);

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        verify(navegacionService)
                .registrarBusquedaRutaAsync(eq(1), eq("La Paz"), eq("Uyuni"), eq(fecha), eq(1));
    }

    private ViajeProgramado crearViajeProgramadoMock() {
        RutaDestino ruta = new RutaDestino();
        ruta.setId(10);
        ruta.setCiudadOrigen("La Paz");
        ruta.setCiudadDestino("Uyuni");
        ruta.setDuracionEstimadaHoras(new BigDecimal("8.00"));
        ruta.setPrecioBase(new BigDecimal("120.00"));
        ruta.setCategoriaTuristica("Aventura");

        Flota flota = new Flota();
        flota.setIdBus(5);
        flota.setTipoBus("Ejecutivo");
        flota.setCapacidadTotalAsientos(40);

        ViajeProgramado viaje = new ViajeProgramado();
        viaje.setId(100);
        viaje.setRutaDestino(ruta);
        viaje.setFlota(flota);
        viaje.setFechaHoraSalida(LocalDateTime.of(2026, 6, 7, 8, 0));
        viaje.setFechaHoraLlegada(LocalDateTime.of(2026, 6, 7, 16, 0));
        viaje.setEstadoViaje("PROGRAMADO");
        return viaje;
    }
}

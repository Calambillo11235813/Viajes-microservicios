package com.agencia.viajes.transaccional.viajes.service;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.navegacion.service.NavegacionService;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de consulta para rutas y horarios visibles al viajero.
 */
@Service
@RequiredArgsConstructor
public class ViajeConsultaService {

    private final ViajeProgramadoRepository viajeProgramadoRepository;
    private final TarifaViajeService tarifaViajeService;
    private final NavegacionService navegacionService;

    /**
     * Consulta viajes programados por origen, destino y fecha.
     *
     * @param origen ciudad de salida.
     * @param destino ciudad de llegada.
     * @param fecha fecha calendario de salida.
     * @param idUsuario identificador del usuario para tracking en DynamoDB (opcional).
     * @return viajes disponibles ordenados por hora de salida.
     * @throws IllegalArgumentException cuando algún criterio obligatorio está vacío.
     */
    @Transactional(readOnly = true)
    public List<ViajeDisponibleResponse> buscarRutasYHorariosDisponibles(
            String origen,
            String destino,
            LocalDate fecha,
            Integer idUsuario) {
        validarCriterios(origen, destino, fecha);

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();

        List<ViajeDisponibleResponse> resultados = viajeProgramadoRepository
                .buscarDisponiblesPorRutaYFecha(origen.trim(), destino.trim(), inicioDia, finDia)
                .stream()
                .map(this::mapearRespuesta)
                .toList();

        navegacionService.registrarBusquedaRutaAsync(
                idUsuario, origen.trim(), destino.trim(), fecha, resultados.size());

        return resultados;
    }

    private void validarCriterios(String origen, String destino, LocalDate fecha) {
        if (origen == null || origen.isBlank()) {
            throw new IllegalArgumentException("La ciudad de origen es obligatoria.");
        }
        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException("La ciudad de destino es obligatoria.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de salida es obligatoria.");
        }
    }

    private ViajeDisponibleResponse mapearRespuesta(ViajeProgramado viaje) {
        RutaDestino ruta = viaje.getRutaDestino();
        Flota flota = viaje.getFlota();
        var precioCalculado = tarifaViajeService.calcularPrecioPorServicio(ruta.getPrecioBase(), flota.getTipoBus());

        return new ViajeDisponibleResponse(
                viaje.getId(),
                ruta.getId(),
                ruta.getCiudadOrigen(),
                ruta.getCiudadDestino(),
                viaje.getFechaHoraSalida().toString(),
                viaje.getFechaHoraLlegada().toString(),
                ruta.getDuracionEstimadaHoras(),
                precioCalculado,
                ruta.getCategoriaTuristica(),
                flota.getIdBus(),
                flota.getTipoBus(),
                flota.getCapacidadTotalAsientos(),
                viaje.getEstadoViaje());
    }
}

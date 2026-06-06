package com.agencia.viajes.transaccional.destinos.service;

import com.agencia.viajes.transaccional.destinos.dto.DestinoTuristicoInfo;
import com.agencia.viajes.transaccional.destinos.dto.DestinoViajesResponse;
import com.agencia.viajes.transaccional.destinos.model.DestinoTuristico;
import com.agencia.viajes.transaccional.destinos.repository.DestinoTuristicoRepository;
import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DestinoTuristicoService {

    private final DestinoTuristicoRepository destinoTuristicoRepository;
    private final ViajeProgramadoRepository viajeProgramadoRepository;

    @Transactional(readOnly = true)
    public DestinoViajesResponse buscarViajesHaciaDestinoTuristico(String nombreDestino) {
        if (nombreDestino == null || nombreDestino.isBlank()) {
            throw new IllegalArgumentException("El nombre del destino turístico es obligatorio.");
        }

        DestinoTuristico destino = destinoTuristicoRepository.findByNombreTuristicoIgnoreCase(nombreDestino.trim())
                .orElseThrow(() -> new IllegalArgumentException("Destino turístico no encontrado en el catálogo: " + nombreDestino));

        List<ViajeProgramado> viajes = viajeProgramadoRepository.buscarDisponiblesHaciaDestinoFuturos(
                destino.getDepartamento(),
                LocalDateTime.now());

        List<ViajeDisponibleResponse> viajesDto = viajes.stream()
                .map(this::mapearRespuestaViaje)
                .toList();

        DestinoTuristicoInfo destinoInfo = new DestinoTuristicoInfo(
                destino.getId(),
                destino.getNombreTuristico(),
                destino.getDepartamento(),
                destino.getDescripcion()
        );

        return new DestinoViajesResponse(destinoInfo, viajesDto);
    }

    private ViajeDisponibleResponse mapearRespuestaViaje(ViajeProgramado viaje) {
        RutaDestino ruta = viaje.getRutaDestino();
        Flota flota = viaje.getFlota();

        return new ViajeDisponibleResponse(
                viaje.getId(),
                ruta.getId(),
                ruta.getCiudadOrigen(),
                ruta.getCiudadDestino(),
                viaje.getFechaHoraSalida().toString(),
                viaje.getFechaHoraLlegada().toString(),
                ruta.getDuracionEstimadaHoras(),
                ruta.getPrecioBase(),
                ruta.getCategoriaTuristica(),
                flota.getIdBus(),
                flota.getTipoBus(),
                flota.getCapacidadTotalAsientos(),
                viaje.getEstadoViaje());
    }
}

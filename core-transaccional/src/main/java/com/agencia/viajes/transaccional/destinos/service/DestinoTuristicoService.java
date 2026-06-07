package com.agencia.viajes.transaccional.destinos.service;

import com.agencia.viajes.transaccional.destinos.dto.DestinoTuristicoInfo;
import com.agencia.viajes.transaccional.destinos.dto.DestinoViajesResponse;
import com.agencia.viajes.transaccional.destinos.dto.OrigenesDestinoTuristicoResponse;
import com.agencia.viajes.transaccional.destinos.model.DestinoTuristico;
import com.agencia.viajes.transaccional.destinos.repository.DestinoTuristicoRepository;
import com.agencia.viajes.transaccional.rutas.repository.RutaDestinoRepository;
import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import com.agencia.viajes.transaccional.viajes.service.TarifaViajeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DestinoTuristicoService {

    private final DestinoTuristicoRepository destinoTuristicoRepository;
    private final ViajeProgramadoRepository viajeProgramadoRepository;
    private final RutaDestinoRepository rutaDestinoRepository;
    private final TarifaViajeService tarifaViajeService;

    @Transactional(readOnly = true)
    public OrigenesDestinoTuristicoResponse listarOrigenesHaciaDestinoTuristico(String nombreDestino) {
        if (nombreDestino == null || nombreDestino.isBlank()) {
            throw new IllegalArgumentException("El nombre del destino turístico es obligatorio.");
        }

        DestinoTuristico destino = destinoTuristicoRepository.findByNombreTuristicoIgnoreCase(nombreDestino.trim())
                .orElseThrow(() -> new IllegalArgumentException("Destino turístico no encontrado en el catálogo: " + nombreDestino));

        String nombreConEspacios = destino.getNombreTuristico().replace("_", " ");
        List<String> origenes = rutaDestinoRepository.findOrigenesDisponiblesHaciaDestino(
                destino.getDepartamento(),
                destino.getNombreTuristico(),
                nombreConEspacios);

        return new OrigenesDestinoTuristicoResponse(destino.getDepartamento(), origenes);
    }

    @Transactional(readOnly = true)
    public DestinoViajesResponse buscarViajesHaciaDestinoTuristico(String nombreDestino, int page, int size, String fechaStr, String origen) {
        if (nombreDestino == null || nombreDestino.isBlank()) {
            throw new IllegalArgumentException("El nombre del destino turístico es obligatorio.");
        }

        DestinoTuristico destino = destinoTuristicoRepository.findByNombreTuristicoIgnoreCase(nombreDestino.trim())
                .orElseThrow(() -> new IllegalArgumentException("Destino turístico no encontrado en el catálogo: " + nombreDestino));

        LocalDateTime inicioDia;
        LocalDateTime finDia;

        if (fechaStr != null && !fechaStr.isBlank()) {
            try {
                LocalDate fecha = LocalDate.parse(fechaStr.trim());
                inicioDia = fecha.atStartOfDay();
                finDia = fecha.plusDays(1).atStartOfDay();
                
                // Si la fecha seleccionada es hoy, ajustar inicioDia a la hora actual
                if (fecha.equals(LocalDate.now())) {
                    inicioDia = LocalDateTime.now();
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha inválido. Se espera YYYY-MM-DD.");
            }
        } else {
            inicioDia = LocalDateTime.now();
            finDia = LocalDateTime.now().plusYears(10);
        }

        String origenFiltro = origen == null || origen.isBlank() ? "" : origen.trim();

        Pageable pageable = PageRequest.of(page, size);

        Page<ViajeProgramado> viajesPage = viajeProgramadoRepository.buscarDisponiblesHaciaDestinoPaginado(
                destino.getDepartamento(),
                origenFiltro,
                inicioDia,
                finDia,
                pageable);

        List<ViajeDisponibleResponse> viajesDto = viajesPage.getContent().stream()
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

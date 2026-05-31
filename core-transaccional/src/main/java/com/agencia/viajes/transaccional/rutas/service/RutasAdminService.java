package com.agencia.viajes.transaccional.rutas.service;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.flotas.repository.FlotaRepository;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.rutas.repository.RutaDestinoRepository;
import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio administrativo para la gestión de rutas y programación de viajes (CU-09).
 */
@Service
@RequiredArgsConstructor
public class RutasAdminService {

    private final RutaDestinoRepository rutaDestinoRepository;
    private final ViajeProgramadoRepository viajeProgramadoRepository;
    private final FlotaRepository flotaRepository;

    // --- Gestión de Rutas ---

    @Transactional(readOnly = true)
    public List<RutaDestino> listarRutas() {
        return rutaDestinoRepository.findAll();
    }

    @Transactional
    public RutaDestino crearRuta(String origen, String destino, BigDecimal duracion, BigDecimal precio, String categoria) {
        RutaDestino ruta = new RutaDestino();
        ruta.setCiudadOrigen(origen);
        ruta.setCiudadDestino(destino);
        ruta.setDuracionEstimadaHoras(duracion);
        ruta.setPrecioBase(precio);
        ruta.setCategoriaTuristica(categoria);
        return rutaDestinoRepository.save(ruta);
    }

    @Transactional
    public RutaDestino actualizarRuta(Integer idRuta, String origen, String destino, BigDecimal duracion, BigDecimal precio, String categoria) {
        RutaDestino ruta = rutaDestinoRepository.findById(idRuta)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));
        
        if (origen != null) ruta.setCiudadOrigen(origen);
        if (destino != null) ruta.setCiudadDestino(destino);
        if (duracion != null) ruta.setDuracionEstimadaHoras(duracion);
        if (precio != null) ruta.setPrecioBase(precio);
        if (categoria != null) ruta.setCategoriaTuristica(categoria);
        
        return rutaDestinoRepository.save(ruta);
    }

    @Transactional
    public boolean eliminarRuta(Integer idRuta) {
        if (!rutaDestinoRepository.existsById(idRuta)) {
            throw new IllegalArgumentException("Ruta no encontrada: " + idRuta);
        }
        rutaDestinoRepository.deleteById(idRuta);
        return true;
    }

    // --- Gestión de Viajes (Horarios) ---

    @Transactional(readOnly = true)
    public List<ViajeDisponibleResponse> listarViajesPorRuta(Integer idRuta) {
        // Obtenemos todos los viajes de una ruta, independientemente de la fecha.
        // Para simplificar usamos findAll y filtramos, aunque en un entorno real habría un query específico.
        return viajeProgramadoRepository.findAll().stream()
                .filter(v -> v.getRutaDestino().getId().equals(idRuta))
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());
    }

    @Transactional
    public ViajeDisponibleResponse programarViaje(Integer idRuta, Integer idBus, String fechaHoraSalidaStr) {
        RutaDestino ruta = rutaDestinoRepository.findById(idRuta)
                .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));
        Flota bus = flotaRepository.findById(idBus)
                .orElseThrow(() -> new IllegalArgumentException("Bus no encontrado: " + idBus));
        
        LocalDateTime salida = LocalDateTime.parse(fechaHoraSalidaStr);
        // Calcular hora de llegada en base a la duración estimada de la ruta
        long minutosDuracion = ruta.getDuracionEstimadaHoras().multiply(new BigDecimal("60")).longValue();
        LocalDateTime llegada = salida.plusMinutes(minutosDuracion);

        // Validación estricta de solapamiento
        validarSolapamiento(idBus, salida, llegada, null);

        ViajeProgramado viaje = new ViajeProgramado();
        viaje.setRutaDestino(ruta);
        viaje.setFlota(bus);
        viaje.setFechaHoraSalida(salida);
        viaje.setFechaHoraLlegada(llegada);
        viaje.setEstadoViaje("PROGRAMADO");

        return mapearRespuesta(viajeProgramadoRepository.save(viaje));
    }

    @Transactional
    public ViajeDisponibleResponse actualizarViajeProgramado(Integer idViaje, Integer idBus, String fechaHoraSalidaStr) {
        ViajeProgramado viaje = viajeProgramadoRepository.findById(idViaje)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado: " + idViaje));
        
        Flota bus = viaje.getFlota();
        if (idBus != null) {
            bus = flotaRepository.findById(idBus)
                    .orElseThrow(() -> new IllegalArgumentException("Bus no encontrado: " + idBus));
            viaje.setFlota(bus);
        }

        if (fechaHoraSalidaStr != null) {
            LocalDateTime salida = LocalDateTime.parse(fechaHoraSalidaStr);
            long minutosDuracion = viaje.getRutaDestino().getDuracionEstimadaHoras().multiply(new BigDecimal("60")).longValue();
            LocalDateTime llegada = salida.plusMinutes(minutosDuracion);
            viaje.setFechaHoraSalida(salida);
            viaje.setFechaHoraLlegada(llegada);
        }

        // Validación estricta de solapamiento excluyendo el viaje actual
        validarSolapamiento(viaje.getFlota().getId(), viaje.getFechaHoraSalida(), viaje.getFechaHoraLlegada(), idViaje);

        return mapearRespuesta(viajeProgramadoRepository.save(viaje));
    }

    @Transactional
    public boolean cancelarViajeProgramado(Integer idViaje) {
        ViajeProgramado viaje = viajeProgramadoRepository.findById(idViaje)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado: " + idViaje));
        viaje.setEstadoViaje("CANCELADO");
        viajeProgramadoRepository.save(viaje);
        return true;
    }

    private void validarSolapamiento(Integer idBus, LocalDateTime inicio, LocalDateTime fin, Integer idViajeExcluido) {
        long overlaps = viajeProgramadoRepository.countSolapamientos(idBus, inicio, fin, idViajeExcluido);
        if (overlaps > 0) {
            throw new IllegalArgumentException("El bus seleccionado ya tiene un viaje programado que se solapa con este horario.");
        }
    }

    private ViajeDisponibleResponse mapearRespuesta(ViajeProgramado viaje) {
        return new ViajeDisponibleResponse(
                viaje.getId(),
                viaje.getRutaDestino().getId(),
                viaje.getRutaDestino().getCiudadOrigen(),
                viaje.getRutaDestino().getCiudadDestino(),
                viaje.getFechaHoraSalida().toString(),
                viaje.getFechaHoraLlegada().toString(),
                viaje.getRutaDestino().getDuracionEstimadaHoras(),
                viaje.getRutaDestino().getPrecioBase(),
                viaje.getRutaDestino().getCategoriaTuristica(),
                viaje.getFlota().getId(),
                viaje.getFlota().getTipoBus(),
                viaje.getFlota().getCapacidadTotalAsientos(),
                viaje.getEstadoViaje()
        );
    }
}

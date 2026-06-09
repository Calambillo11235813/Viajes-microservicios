package com.agencia.viajes.transaccional.rutas.service;

import com.agencia.viajes.transaccional.flotas.model.Flota;
import com.agencia.viajes.transaccional.flotas.repository.FlotaRepository;
import com.agencia.viajes.transaccional.rutas.model.RutaDestino;
import com.agencia.viajes.transaccional.rutas.repository.RutaDestinoRepository;
import com.agencia.viajes.transaccional.viajes.dto.PaginaViajesResponse;
import com.agencia.viajes.transaccional.viajes.dto.ViajeDisponibleResponse;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import com.agencia.viajes.transaccional.notificaciones.service.NotificacionService;
import com.agencia.viajes.transaccional.viajes.service.TarifaViajeService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio administrativo para la gestión de rutas y programación de viajes (CU-09).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RutasAdminService {

    private static final DateTimeFormatter HORARIO_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RutaDestinoRepository rutaDestinoRepository;
    private final ViajeProgramadoRepository viajeProgramadoRepository;
    private final FlotaRepository flotaRepository;
    private final TarifaViajeService tarifaViajeService;
    private final NotificacionService notificacionService;

    // --- Gestión de Rutas ---

    /**
     * Lista todas las rutas (Paginado).
     */
    @Transactional(readOnly = true)
    public com.agencia.viajes.transaccional.rutas.dto.PaginaRutasResponse listarRutas(int pagina, int tamanio) {
        Pageable pageable = PageRequest.of(pagina, tamanio);
        Page<RutaDestino> page = rutaDestinoRepository.findAll(pageable);
        return new com.agencia.viajes.transaccional.rutas.dto.PaginaRutasResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.hasNext()
        );
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
    public PaginaViajesResponse listarViajesPorRuta(Integer idRuta, int pagina, int tamanio) {
        var pageable = org.springframework.data.domain.PageRequest.of(pagina, tamanio,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "fechaHoraSalida"));
        var page = viajeProgramadoRepository.buscarPorRutaPaginado(idRuta, pageable);

        var contenido = page.getContent().stream()
                .map(this::mapearRespuesta)
                .collect(Collectors.toList());

        return new PaginaViajesResponse(
                contenido,
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.hasNext());
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

        LocalDateTime fechaAnterior = viaje.getFechaHoraSalida();
        Integer busAnterior = viaje.getFlota().getIdBus();
        String origen = viaje.getRutaDestino().getCiudadOrigen();
        String destino = viaje.getRutaDestino().getCiudadDestino();

        LocalDateTime salidaNueva = fechaAnterior;
        if (fechaHoraSalidaStr != null) {
            salidaNueva = LocalDateTime.parse(fechaHoraSalidaStr);
        }

        Integer busNuevo = busAnterior;
        Flota bus = viaje.getFlota();
        if (idBus != null) {
            busNuevo = idBus;
            bus = flotaRepository.findById(idBus)
                    .orElseThrow(() -> new IllegalArgumentException("Bus no encontrado: " + idBus));
            viaje.setFlota(bus);
        }

        boolean cambioHorario = !salidaNueva.equals(fechaAnterior);
        boolean cambioBus = !busNuevo.equals(busAnterior);

        if (!cambioHorario && !cambioBus) {
            log.info("[Viajes Admin] Sin cambios en viaje #{}", idViaje);
            return mapearRespuesta(viaje);
        }

        if (cambioHorario) {
            long minutosDuracion = viaje.getRutaDestino().getDuracionEstimadaHoras().multiply(new BigDecimal("60")).longValue();
            LocalDateTime llegada = salidaNueva.plusMinutes(minutosDuracion);
            viaje.setFechaHoraSalida(salidaNueva);
            viaje.setFechaHoraLlegada(llegada);
        }

        // Validación estricta de solapamiento excluyendo el viaje actual
        validarSolapamiento(viaje.getFlota().getIdBus(), viaje.getFechaHoraSalida(), viaje.getFechaHoraLlegada(), idViaje);

        ViajeDisponibleResponse respuesta = mapearRespuesta(viajeProgramadoRepository.save(viaje));

        if (fechaHoraSalidaStr != null && cambioHorario) {
            String mensaje = String.format(
                    "El viaje %s → %s cambió su hora de salida de %s a %s.",
                    origen,
                    destino,
                    fechaAnterior,
                    viaje.getFechaHoraSalida());
            String datosExtra = String.format(
                    "{\"idViaje\":%d,\"fechaAnterior\":\"%s\",\"fechaNueva\":\"%s\"}",
                    idViaje,
                    fechaAnterior,
                    viaje.getFechaHoraSalida());
            notificacionService.notificarAutomaticaPorViaje(
                    idViaje,
                    "CAMBIO_HORARIO",
                    "Cambio de horario de viaje",
                    mensaje,
                    datosExtra);
        }

        return respuesta;
    }

    @Transactional
    public boolean cancelarViajeProgramado(Integer idViaje) {
        ViajeProgramado viaje = viajeProgramadoRepository.findById(idViaje)
                .orElseThrow(() -> new IllegalArgumentException("Viaje no encontrado: " + idViaje));
        String origen = viaje.getRutaDestino().getCiudadOrigen();
        String destino = viaje.getRutaDestino().getCiudadDestino();
        viaje.setEstadoViaje("CANCELADO");
        viajeProgramadoRepository.save(viaje);

        String mensaje = String.format(
                "El viaje %s → %s programado para %s ha sido cancelado.",
                origen,
                destino,
                viaje.getFechaHoraSalida());
        String datosExtra = String.format("{\"idViaje\":%d,\"estadoViaje\":\"CANCELADO\"}", idViaje);
        notificacionService.notificarAutomaticaPorViaje(
                idViaje,
                "CANCELACION",
                "Viaje cancelado",
                mensaje,
                datosExtra);

        return true;
    }

    private void validarSolapamiento(Integer idBus, LocalDateTime inicio, LocalDateTime fin, Integer idViajeExcluido) {
        List<ViajeProgramado> conflictos = idViajeExcluido == null
                ? viajeProgramadoRepository.buscarSolapamientos(idBus, inicio, fin)
                : viajeProgramadoRepository.buscarSolapamientosExcluyendo(idBus, inicio, fin, idViajeExcluido);

        if (conflictos.isEmpty()) {
            return;
        }

        ViajeProgramado conflicto = conflictos.get(0);
        String mensaje = String.format(
                "El bus %d ya tiene el viaje #%d (%s → %s, %s a %s) que se solapa con el horario solicitado (%s a %s). "
                        + "Elija otro bus u otro horario.",
                idBus,
                conflicto.getId(),
                conflicto.getRutaDestino().getCiudadOrigen(),
                conflicto.getRutaDestino().getCiudadDestino(),
                conflicto.getFechaHoraSalida().format(HORARIO_FMT),
                conflicto.getFechaHoraLlegada().format(HORARIO_FMT),
                inicio.format(HORARIO_FMT),
                fin.format(HORARIO_FMT));
        log.warn("[Viajes Admin] Solapamiento detectado: {}", mensaje);
        throw new IllegalArgumentException(mensaje);
    }

    private ViajeDisponibleResponse mapearRespuesta(ViajeProgramado viaje) {
        BigDecimal precioCalculado = tarifaViajeService.calcularPrecioPorServicio(
                viaje.getRutaDestino().getPrecioBase(),
                viaje.getFlota().getTipoBus());

        return new ViajeDisponibleResponse(
                viaje.getId(),
                viaje.getRutaDestino().getId(),
                viaje.getRutaDestino().getCiudadOrigen(),
                viaje.getRutaDestino().getCiudadDestino(),
                viaje.getFechaHoraSalida().toString(),
                viaje.getFechaHoraLlegada().toString(),
                viaje.getRutaDestino().getDuracionEstimadaHoras(),
                precioCalculado,
                viaje.getRutaDestino().getCategoriaTuristica(),
                viaje.getFlota().getIdBus(),
                viaje.getFlota().getTipoBus(),
                viaje.getFlota().getCapacidadTotalAsientos(),
                viaje.getEstadoViaje()
        );
    }
}

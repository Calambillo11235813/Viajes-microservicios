package com.agencia.viajes.transaccional.reservas.service;

import com.agencia.viajes.transaccional.reservas.dto.HistorialViajeResponse;
import com.agencia.viajes.transaccional.reservas.dto.PaginaHistorialResponse;
import com.agencia.viajes.transaccional.reservas.repository.ReservaRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para consultar el historial de viajes de un usuario.
 */
@Service
@RequiredArgsConstructor
public class HistorialViajesService {

    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public List<HistorialViajeResponse> obtenerHistorialViajes(Integer idUsuario) {
        return reservaRepository.buscarHistorialPorUsuario(idUsuario).stream()
                .map(reserva -> HistorialViajeResponse.builder()
                        .idReserva(reserva.getId())
                        .idViaje(reserva.getViajeProgramado().getId())
                        .ciudadOrigen(reserva.getViajeProgramado().getRutaDestino().getCiudadOrigen())
                        .ciudadDestino(reserva.getViajeProgramado().getRutaDestino().getCiudadDestino())
                        .fechaHoraSalida(reserva.getViajeProgramado().getFechaHoraSalida().toString())
                        .fechaCreacion(reserva.getFechaCreacion().toString())
                        .estadoReserva(reserva.getEstadoReserva())
                        .montoTotalPagado(reserva.getMontoTotalPagado())
                        .cantidadPasajeros(reserva.getCantidadPasajeros())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaginaHistorialResponse obtenerHistorialViajesPaginado(Integer idUsuario, int pagina, int tamanio) {
        var pageable = PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
        var page = reservaRepository.buscarHistorialPorUsuarioPaginado(idUsuario, pageable);

        var contenido = page.getContent().stream()
                .map(reserva -> HistorialViajeResponse.builder()
                        .idReserva(reserva.getId())
                        .idViaje(reserva.getViajeProgramado().getId())
                        .ciudadOrigen(reserva.getViajeProgramado().getRutaDestino().getCiudadOrigen())
                        .ciudadDestino(reserva.getViajeProgramado().getRutaDestino().getCiudadDestino())
                        .fechaHoraSalida(reserva.getViajeProgramado().getFechaHoraSalida().toString())
                        .fechaCreacion(reserva.getFechaCreacion().toString())
                        .estadoReserva(reserva.getEstadoReserva())
                        .montoTotalPagado(reserva.getMontoTotalPagado())
                        .cantidadPasajeros(reserva.getCantidadPasajeros())
                        .build())
                .toList();

        return new PaginaHistorialResponse(
                contenido,
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.hasNext());
    }
}

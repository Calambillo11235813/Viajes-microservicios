package com.agencia.viajes.transaccional.reservas.service;

import com.agencia.viajes.transaccional.reservas.dto.AsientoEstadoResponse;
import com.agencia.viajes.transaccional.reservas.dto.ReservaProvisionalResponse;
import com.agencia.viajes.transaccional.reservas.model.BoletoAsiento;
import com.agencia.viajes.transaccional.reservas.model.Reserva;
import com.agencia.viajes.transaccional.reservas.repository.BoletoAsientoRepository;
import com.agencia.viajes.transaccional.reservas.repository.ReservaRepository;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import com.agencia.viajes.transaccional.viajes.repository.ViajeProgramadoRepository;
import com.agencia.viajes.transaccional.viajes.service.TarifaViajeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para consultar ocupación y reservar asientos de forma transaccional.
 */
@Service
@RequiredArgsConstructor
public class ReservaAsientoService {

    private static final String ESTADO_VIAJE_PROGRAMADO = "PROGRAMADO";
    private static final String ESTADO_RESERVA_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_BOLETO_RESERVADO = "RESERVADO";
    private static final String TIPO_PASAJERO_ADULTO = "ADULTO";

    private final ViajeProgramadoRepository viajeProgramadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final BoletoAsientoRepository boletoAsientoRepository;
    private final TarifaViajeService tarifaViajeService;

    /**
     * Devuelve el mapa simple de asientos de un viaje.
     *
     * @param idViaje identificador del viaje programado.
     * @return asientos generados según la capacidad del bus, con su ocupación.
     * @throws IllegalArgumentException si el viaje no existe.
     */
    @Transactional(readOnly = true)
    public List<AsientoEstadoResponse> obtenerMapaAsientos(Integer idViaje) {
        ViajeProgramado viaje = viajeProgramadoRepository.findById(idViaje)
                .orElseThrow(() -> new IllegalArgumentException("El viaje programado no existe."));

        Set<String> ocupados = new HashSet<>(boletoAsientoRepository.buscarNumerosOcupadosPorViaje(idViaje)
                .stream()
                .map(asiento -> asiento.trim().toUpperCase())
                .toList());

        return IntStream.rangeClosed(1, viaje.getFlota().getCapacidadTotalAsientos())
                .mapToObj(numero -> String.valueOf(numero))
                .map(numero -> new AsientoEstadoResponse(numero, ocupados.contains(numero)))
                .toList();
    }

    /**
     * Reserva provisionalmente un asiento para un usuario.
     *
     * @param idUsuario identificador del usuario cliente.
     * @param idViaje identificador del viaje programado.
     * @param numeroAsiento asiento seleccionado por el cliente.
     * @param nombrePasajero nombre impreso para el pasajero.
     * @param tipoPasajero tipo de pasajero, por defecto ADULTO si llega vacío.
     * @return datos de la reserva provisional creada.
     * @throws IllegalArgumentException si los datos no son válidos o el asiento está ocupado.
     */
    @Transactional
    public ReservaProvisionalResponse seleccionarAsientoYReservar(
            Integer idUsuario,
            Integer idViaje,
            String numeroAsiento,
            String nombrePasajero,
            String tipoPasajero) {
        validarDatosReserva(idUsuario, idViaje, numeroAsiento, nombrePasajero);

        ViajeProgramado viaje = viajeProgramadoRepository.buscarPorIdConBloqueo(idViaje)
                .orElseThrow(() -> new IllegalArgumentException("El viaje programado no existe."));
        validarViajeReservable(viaje);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

        String asientoNormalizado = numeroAsiento.trim().toUpperCase();
        validarAsientoDentroDeCapacidad(asientoNormalizado, viaje.getFlota().getCapacidadTotalAsientos());
        validarAsientoDisponible(idViaje, asientoNormalizado);

        LocalDateTime ahora = LocalDateTime.now();
        Reserva reserva = crearReservaPendiente(usuario, viaje, ahora);
        BoletoAsiento boleto = crearBoletoReservado(reserva, asientoNormalizado, nombrePasajero, tipoPasajero, ahora);

        BigDecimal montoEstimado = tarifaViajeService.calcularPrecioPorServicio(
                viaje.getRutaDestino().getPrecioBase(),
                viaje.getFlota().getTipoBus());

        return mapearRespuesta(reserva, boleto, montoEstimado);
    }

    private void validarDatosReserva(Integer idUsuario, Integer idViaje, String numeroAsiento, String nombrePasajero) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (idViaje == null) {
            throw new IllegalArgumentException("El viaje es obligatorio.");
        }
        if (numeroAsiento == null || numeroAsiento.isBlank()) {
            throw new IllegalArgumentException("El asiento es obligatorio.");
        }
        if (nombrePasajero == null || nombrePasajero.isBlank()) {
            throw new IllegalArgumentException("El nombre del pasajero es obligatorio.");
        }
    }

    private void validarViajeReservable(ViajeProgramado viaje) {
        if (!ESTADO_VIAJE_PROGRAMADO.equals(viaje.getEstadoViaje())) {
            throw new IllegalArgumentException("Solo se pueden reservar asientos en viajes programados.");
        }
    }

    private void validarAsientoDentroDeCapacidad(String numeroAsiento, Integer capacidadTotal) {
        String digitos = numeroAsiento.replaceAll("[^0-9]", "");
        if (digitos.isBlank()) {
            throw new IllegalArgumentException("El asiento debe contener un número válido.");
        }

        int numeroBase = Integer.parseInt(digitos);
        if (numeroBase < 1 || numeroBase > capacidadTotal) {
            throw new IllegalArgumentException("El asiento seleccionado está fuera de la capacidad del bus.");
        }
    }

    private void validarAsientoDisponible(Integer idViaje, String asientoNormalizado) {
        if (boletoAsientoRepository.existeAsientoOcupado(idViaje, asientoNormalizado)) {
            throw new IllegalArgumentException("El asiento seleccionado ya está ocupado.");
        }
    }

    private Reserva crearReservaPendiente(Usuario usuario, ViajeProgramado viaje, LocalDateTime ahora) {
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setViajeProgramado(viaje);
        reserva.setFechaCreacion(ahora);
        reserva.setEstadoReserva(ESTADO_RESERVA_PENDIENTE);
        reserva.setMontoTotalPagado(BigDecimal.ZERO);
        reserva.setCantidadPasajeros(1);
        return reservaRepository.save(reserva);
    }

    private BoletoAsiento crearBoletoReservado(
            Reserva reserva,
            String numeroAsiento,
            String nombrePasajero,
            String tipoPasajero,
            LocalDateTime ahora) {
        BoletoAsiento boleto = new BoletoAsiento();
        boleto.setReserva(reserva);
        boleto.setNumeroAsiento(numeroAsiento);
        boleto.setNombrePasajero(nombrePasajero.trim());
        boleto.setFechaEmision(ahora);
        boleto.setEstadoBoleto(ESTADO_BOLETO_RESERVADO);
        boleto.setTipoPasajero(normalizarTipoPasajero(tipoPasajero));
        return boletoAsientoRepository.save(boleto);
    }

    private String normalizarTipoPasajero(String tipoPasajero) {
        if (tipoPasajero == null || tipoPasajero.isBlank()) {
            return TIPO_PASAJERO_ADULTO;
        }
        return tipoPasajero.trim().toUpperCase();
    }

    private ReservaProvisionalResponse mapearRespuesta(
            Reserva reserva,
            BoletoAsiento boleto,
            BigDecimal montoEstimado) {
        return new ReservaProvisionalResponse(
                reserva.getId(),
                boleto.getId(),
                reserva.getViajeProgramado().getId(),
                reserva.getUsuario().getId(),
                boleto.getNumeroAsiento(),
                boleto.getNombrePasajero(),
                boleto.getTipoPasajero(),
                reserva.getEstadoReserva(),
                boleto.getEstadoBoleto(),
                montoEstimado,
                reserva.getFechaCreacion().toString());
    }
}

package com.agencia.viajes.transaccional.reservas.service;

import com.agencia.viajes.transaccional.pagos.model.Pago;
import com.agencia.viajes.transaccional.pagos.repository.PagoRepository;
import com.agencia.viajes.transaccional.reservas.dto.ReservaCanceladaResponse;
import com.agencia.viajes.transaccional.reservas.model.BoletoAsiento;
import com.agencia.viajes.transaccional.reservas.model.Reserva;
import com.agencia.viajes.transaccional.reservas.repository.BoletoAsientoRepository;
import com.agencia.viajes.transaccional.reservas.repository.ReservaRepository;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la cancelación de reservas (CU-06).
 * Valida plazos permitidos, anula la reserva y libera los asientos ocupados.
 */
@Service
@RequiredArgsConstructor
public class CancelacionReservaService {

    /** Horas mínimas de anticipación respecto a la salida del viaje para permitir la cancelación. */
    private static final long HORAS_LIMITE_CANCELACION = 24;

    private static final Set<String> ESTADOS_CANCELABLES = Set.of("PENDIENTE", "CONFIRMADA");
    private static final String ESTADO_RESERVA_CANCELADA = "CANCELADA";
    private static final String ESTADO_BOLETO_ANULADO = "ANULADO";
    private static final String ESTADO_PAGO_REEMBOLSADO = "REEMBOLSADO";
    private static final String ESTADO_PAGO_CONFIRMADO = "CONFIRMADO";

    private final ReservaRepository reservaRepository;
    private final BoletoAsientoRepository boletoAsientoRepository;
    private final PagoRepository pagoRepository;

    /**
     * Cancela una reserva existente, libera sus asientos y reembolsa el pago si corresponde.
     *
     * @param idReserva identificador de la reserva a cancelar.
     * @param idUsuario identificador del usuario solicitante (debe ser el dueño de la reserva).
     * @return datos de la cancelación realizada.
     * @throws IllegalArgumentException si la reserva no existe, no pertenece al usuario,
     *         no está en estado cancelable o el plazo de cancelación expiró.
     */
    @Transactional
    public ReservaCanceladaResponse cancelarReserva(Integer idReserva, Integer idUsuario) {
        validarParametros(idReserva, idUsuario);

        Reserva reserva = reservaRepository.buscarPorIdConBloqueoParaCancelacion(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe."));

        validarPropietario(reserva, idUsuario);
        validarEstadoCancelable(reserva);
        validarPlazoCancelacion(reserva);

        List<BoletoAsiento> boletos = anularBoletos(idReserva);
        BigDecimal montoReembolsado = reembolsarPagoSiExiste(idReserva);
        marcarReservaCancelada(reserva);

        return mapearRespuesta(reserva, boletos.size(), montoReembolsado);
    }

    private void validarParametros(Integer idReserva, Integer idUsuario) {
        if (idReserva == null) {
            throw new IllegalArgumentException("El identificador de la reserva es obligatorio.");
        }
        if (idUsuario == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio.");
        }
    }

    private void validarPropietario(Reserva reserva, Integer idUsuario) {
        if (!reserva.getUsuario().getId().equals(idUsuario)) {
            throw new IllegalArgumentException("La reserva no pertenece al usuario indicado.");
        }
    }

    private void validarEstadoCancelable(Reserva reserva) {
        if (!ESTADOS_CANCELABLES.contains(reserva.getEstadoReserva())) {
            throw new IllegalArgumentException(
                    "Solo se pueden cancelar reservas en estado PENDIENTE o CONFIRMADA.");
        }
    }

    /**
     * Verifica que falten al menos {@value #HORAS_LIMITE_CANCELACION} horas
     * para la salida del viaje.
     */
    private void validarPlazoCancelacion(Reserva reserva) {
        ViajeProgramado viaje = reserva.getViajeProgramado();
        LocalDateTime limiteCancelacion = viaje.getFechaHoraSalida()
                .minusHours(HORAS_LIMITE_CANCELACION);

        if (LocalDateTime.now().isAfter(limiteCancelacion)) {
            throw new IllegalArgumentException(
                    "No se puede cancelar la reserva. El plazo mínimo de "
                            + HORAS_LIMITE_CANCELACION
                            + " horas antes de la salida ya expiró.");
        }
    }

    private List<BoletoAsiento> anularBoletos(Integer idReserva) {
        List<BoletoAsiento> boletos = boletoAsientoRepository.findByReservaId(idReserva);
        for (BoletoAsiento boleto : boletos) {
            boleto.setEstadoBoleto(ESTADO_BOLETO_ANULADO);
            boletoAsientoRepository.save(boleto);
        }
        return boletos;
    }

    private BigDecimal reembolsarPagoSiExiste(Integer idReserva) {
        Optional<Pago> pagoOpt = pagoRepository.findByReservaId(idReserva);
        if (pagoOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Pago pago = pagoOpt.get();
        if (!ESTADO_PAGO_CONFIRMADO.equals(pago.getEstadoPago())) {
            return BigDecimal.ZERO;
        }
        pago.setEstadoPago(ESTADO_PAGO_REEMBOLSADO);
        pagoRepository.save(pago);
        return pago.getMontoTransaccion();
    }

    private void marcarReservaCancelada(Reserva reserva) {
        reserva.setEstadoReserva(ESTADO_RESERVA_CANCELADA);
        reserva.setMontoTotalPagado(BigDecimal.ZERO);
        reservaRepository.save(reserva);
    }

    private ReservaCanceladaResponse mapearRespuesta(
            Reserva reserva,
            int boletosAnulados,
            BigDecimal montoReembolsado) {
        ViajeProgramado viaje = reserva.getViajeProgramado();
        return new ReservaCanceladaResponse(
                reserva.getId(),
                reserva.getUsuario().getId(),
                viaje.getId(),
                viaje.getRutaDestino().getCiudadOrigen(),
                viaje.getRutaDestino().getCiudadDestino(),
                reserva.getEstadoReserva(),
                montoReembolsado.compareTo(BigDecimal.ZERO) > 0
                        ? ESTADO_PAGO_REEMBOLSADO
                        : "SIN_PAGO",
                boletosAnulados,
                montoReembolsado,
                LocalDateTime.now().toString());
    }
}

package com.agencia.viajes.transaccional.pagos.service;

import com.agencia.viajes.transaccional.pagos.dto.PagoConfirmadoResponse;
import com.agencia.viajes.transaccional.pagos.event.PagoConfirmadoEventPublisher;
import com.agencia.viajes.transaccional.pagos.model.Pago;
import com.agencia.viajes.transaccional.pagos.repository.PagoRepository;
import com.agencia.viajes.transaccional.reservas.model.BoletoAsiento;
import com.agencia.viajes.transaccional.reservas.model.Reserva;
import com.agencia.viajes.transaccional.reservas.repository.BoletoAsientoRepository;
import com.agencia.viajes.transaccional.reservas.repository.ReservaRepository;
import com.agencia.viajes.transaccional.viajes.service.TarifaViajeService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Servicio transaccional para registrar pagos acreditados.
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    private static final String ESTADO_RESERVA_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_RESERVA_CONFIRMADA = "CONFIRMADA";
    private static final String ESTADO_PAGO_CONFIRMADO = "CONFIRMADO";
    private static final String ESTADO_BOLETO_EMITIDO = "EMITIDO";
    private static final Set<String> METODOS_PERMITIDOS = Set.of("QR", "TRANSFERENCIA");

    private final ReservaRepository reservaRepository;
    private final BoletoAsientoRepository boletoAsientoRepository;
    private final PagoRepository pagoRepository;
    private final PagoConfirmadoEventPublisher pagoConfirmadoEventPublisher;
    private final TarifaViajeService tarifaViajeService;

    /**
     * Registra un pago acreditado y confirma la reserva.
     *
     * @param idReserva reserva pendiente a pagar.
     * @param metodoPagoUsado método usado por el cliente.
     * @param montoTransaccion monto acreditado.
     * @param acreditado resultado de verificación de la pasarela o banco.
     * @param cuponDescuentoAplicado cupón opcional aplicado al pago.
     * @return pago confirmado.
     * @throws IllegalArgumentException si la reserva no puede pagarse.
     */
    @Transactional
    public PagoConfirmadoResponse realizarPago(
            Integer idReserva,
            String metodoPagoUsado,
            BigDecimal montoTransaccion,
            Boolean acreditado,
            String cuponDescuentoAplicado) {
        validarSolicitud(idReserva, metodoPagoUsado, montoTransaccion, acreditado);

        Reserva reserva = reservaRepository.buscarPorIdConBloqueoParaPago(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("La reserva no existe."));
        validarReservaPendiente(reserva);
        validarPagoDuplicado(idReserva);

        BigDecimal montoUnitario = tarifaViajeService.calcularPrecioPorServicio(
                reserva.getViajeProgramado().getRutaDestino().getPrecioBase(),
                reserva.getViajeProgramado().getFlota().getTipoBus());
        BigDecimal montoEsperado = montoUnitario.multiply(BigDecimal.valueOf(reserva.getCantidadPasajeros()));
        validarMonto(montoTransaccion, montoEsperado);

        Pago pago = crearPagoConfirmado(reserva, metodoPagoUsado, montoTransaccion, cuponDescuentoAplicado);
        confirmarReservaYBoletos(reserva);
        registrarEventoDespuesDelCommit(pago);

        return mapearRespuesta(pago);
    }

    private void validarSolicitud(
            Integer idReserva,
            String metodoPagoUsado,
            BigDecimal montoTransaccion,
            Boolean acreditado) {
        if (idReserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        if (metodoPagoUsado == null || metodoPagoUsado.isBlank()) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }
        if (!METODOS_PERMITIDOS.contains(metodoPagoUsado.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("El método de pago debe ser QR o TRANSFERENCIA.");
        }
        if (montoTransaccion == null || montoTransaccion.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la transacción debe ser mayor a cero.");
        }
        if (!Boolean.TRUE.equals(acreditado)) {
            throw new IllegalArgumentException("El pago no fue acreditado por la entidad financiera.");
        }
    }

    private void validarReservaPendiente(Reserva reserva) {
        if (!ESTADO_RESERVA_PENDIENTE.equals(reserva.getEstadoReserva())) {
            throw new IllegalArgumentException("Solo se pueden pagar reservas pendientes.");
        }
    }

    private void validarPagoDuplicado(Integer idReserva) {
        if (pagoRepository.existsByReservaId(idReserva)) {
            throw new IllegalArgumentException("La reserva ya tiene un pago registrado.");
        }
    }

    private void validarMonto(BigDecimal montoTransaccion, BigDecimal montoEsperado) {
        if (montoTransaccion.compareTo(montoEsperado) != 0) {
            throw new IllegalArgumentException("El monto acreditado no coincide con el total de la reserva.");
        }
    }

    private Pago crearPagoConfirmado(
            Reserva reserva,
            String metodoPagoUsado,
            BigDecimal montoTransaccion,
            String cuponDescuentoAplicado) {
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setFechaPago(LocalDateTime.now());
        pago.setMontoTransaccion(montoTransaccion);
        pago.setMetodoPagoUsado(metodoPagoUsado.trim().toUpperCase(Locale.ROOT));
        pago.setCuponDescuentoAplicado(normalizarCupon(cuponDescuentoAplicado));
        pago.setEstadoPago(ESTADO_PAGO_CONFIRMADO);
        return pagoRepository.save(pago);
    }

    private String normalizarCupon(String cuponDescuentoAplicado) {
        if (cuponDescuentoAplicado == null || cuponDescuentoAplicado.isBlank()) {
            return null;
        }
        return cuponDescuentoAplicado.trim().toUpperCase(Locale.ROOT);
    }

    private void confirmarReservaYBoletos(Reserva reserva) {
        reserva.setEstadoReserva(ESTADO_RESERVA_CONFIRMADA);
        BigDecimal montoUnitario = tarifaViajeService.calcularPrecioPorServicio(
                reserva.getViajeProgramado().getRutaDestino().getPrecioBase(),
                reserva.getViajeProgramado().getFlota().getTipoBus());
        reserva.setMontoTotalPagado(montoUnitario.multiply(BigDecimal.valueOf(reserva.getCantidadPasajeros())));
        reservaRepository.save(reserva);

        for (BoletoAsiento boleto : boletoAsientoRepository.findByReservaId(reserva.getId())) {
            boleto.setEstadoBoleto(ESTADO_BOLETO_EMITIDO);
            boletoAsientoRepository.save(boleto);
        }
    }

    private void registrarEventoDespuesDelCommit(Pago pago) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    pagoConfirmadoEventPublisher.publicarPagoConfirmado(pago);
                } catch (Exception e) {
                    // Redis puede no estar disponible; el pago ya fue confirmado en BD.
                    System.err.println("[WARN] No se pudo publicar evento de pago a Redis: " + e.getMessage());
                }
            }
        });
    }

    private PagoConfirmadoResponse mapearRespuesta(Pago pago) {
        Reserva reserva = pago.getReserva();
        return new PagoConfirmadoResponse(
                pago.getId(),
                reserva.getId(),
                reserva.getUsuario().getId(),
                reserva.getViajeProgramado().getId(),
                pago.getMontoTransaccion(),
                pago.getMetodoPagoUsado(),
                pago.getEstadoPago(),
                reserva.getEstadoReserva(),
                pago.getFechaPago().toString(),
                true);
    }
}

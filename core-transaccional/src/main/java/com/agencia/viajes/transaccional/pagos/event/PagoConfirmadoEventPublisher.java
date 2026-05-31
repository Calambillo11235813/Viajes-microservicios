package com.agencia.viajes.transaccional.pagos.event;

import com.agencia.viajes.transaccional.pagos.model.Pago;
import com.agencia.viajes.transaccional.reservas.model.Reserva;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de pago confirmado para microservicios consumidores.
 */
@Component
@RequiredArgsConstructor
public class PagoConfirmadoEventPublisher {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.events.pagos-confirmados-channel:pagos.confirmados}")
    private String pagosConfirmadosChannel;

    /**
     * Emite un evento compacto de pago confirmado en Redis.
     *
     * @param pago pago confirmado.
     */
    public void publicarPagoConfirmado(Pago pago) {
        Reserva reserva = pago.getReserva();
        String payload = """
                {"eventType":"PAGO_CONFIRMADO","idPago":%d,"idReserva":%d,"idUsuario":%d,"idViaje":%d,"montoTransaccion":%s,"metodoPago":"%s","fechaPago":"%s"}
                """.formatted(
                pago.getId(),
                reserva.getId(),
                reserva.getUsuario().getId(),
                reserva.getViajeProgramado().getId(),
                pago.getMontoTransaccion().toPlainString(),
                pago.getMetodoPagoUsado().toUpperCase(Locale.ROOT),
                pago.getFechaPago());

        redisTemplate.convertAndSend(pagosConfirmadosChannel, payload.strip());
    }
}

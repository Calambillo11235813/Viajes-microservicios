package com.agencia.viajes.transaccional.pagos.event;

import com.agencia.viajes.transaccional.pagos.model.Pago;
import com.agencia.viajes.transaccional.reservas.model.BoletoAsiento;
import com.agencia.viajes.transaccional.reservas.model.Reserva;
import com.agencia.viajes.transaccional.usuarios.model.Usuario;
import com.agencia.viajes.transaccional.viajes.model.ViajeProgramado;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica un evento enriquecido de emisión de boletos para el microservicio de
 * automatización y auditoría. El payload incluye un boleto por cada asiento de
 * la reserva para que se genere un PDF independiente por asiento.
 */
@Component
@RequiredArgsConstructor
public class BoletosEmitirEventPublisher {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final StringRedisTemplate redisTemplate;

    @Value("${app.events.boletos-emitir-channel:boletos.emitir}")
    private String boletosEmitirChannel;

    /**
     * Construye el JSON del evento de emisión de boletos.
     *
     * <p>Debe invocarse dentro de la transacción para que las asociaciones LAZY
     * de JPA (usuario, viaje, ruta) sigan disponibles.
     *
     * @param pago pago confirmado.
     * @param boletos boletos asociados a la reserva.
     * @return payload JSON listo para publicar.
     */
    public String construirPayload(Pago pago, List<BoletoAsiento> boletos) {
        Reserva reserva = pago.getReserva();
        Usuario usuario = reserva.getUsuario();
        ViajeProgramado viaje = reserva.getViajeProgramado();
        LocalDateTime salida = viaje.getFechaHoraSalida();

        StringJoiner boletosJson = new StringJoiner(",", "[", "]");
        for (BoletoAsiento boleto : boletos) {
            boletosJson.add("""
                    {"idBoleto":%d,"asiento":"%s","nombre":"%s","tipoPasajero":"%s"}"""
                    .formatted(
                            boleto.getId(),
                            escapar(boleto.getNumeroAsiento()),
                            escapar(boleto.getNombrePasajero()),
                            escapar(boleto.getTipoPasajero())));
        }

        return """
                {"eventType":"BOLETOS_EMITIR","idPago":%d,"idReserva":%d,"email":"%s","origen":"%s","destino":"%s","fecha":"%s","hora":"%s","boletos":%s}"""
                .formatted(
                        pago.getId(),
                        reserva.getId(),
                        escapar(usuario.getEmail()),
                        escapar(viaje.getRutaDestino().getCiudadOrigen()),
                        escapar(viaje.getRutaDestino().getCiudadDestino()),
                        salida.format(FORMATO_FECHA),
                        salida.format(FORMATO_HORA),
                        boletosJson.toString());
    }

    /**
     * Emite el evento de emisión de boletos en Redis.
     *
     * @param payload JSON previamente construido con {@link #construirPayload}.
     */
    public void publicar(String payload) {
        redisTemplate.convertAndSend(boletosEmitirChannel, payload);
    }

    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}

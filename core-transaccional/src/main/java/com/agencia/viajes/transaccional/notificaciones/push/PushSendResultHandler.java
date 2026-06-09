package com.agencia.viajes.transaccional.notificaciones.push;

import com.agencia.viajes.transaccional.notificaciones.service.DispositivoPushService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Procesa resultados del proveedor push: tokens inválidos y fallos definitivos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushSendResultHandler {

    private final DispositivoPushService dispositivoPushService;

    public void procesarResultado(List<PushMessage> mensajes, PushDeliveryResult resultado) {
        if (resultado.exito()) {
            procesarTokensInvalidos(resultado.tokensInvalidos());
            if (!resultado.tokensInvalidos().isEmpty()) {
                log.info(
                        "Push entregado con {} token(s) inválido(s) tras {} intento(s)",
                        resultado.tokensInvalidos().size(),
                        resultado.intentosRealizados());
            }
            return;
        }

        registrarFalloDefinitivo(mensajes, resultado);
    }

    public void procesarTokensInvalidos(List<String> tokensInvalidos) {
        for (String token : tokensInvalidos) {
            log.info("Desactivando token push inválido: {}", enmascararToken(token));
            dispositivoPushService.desactivarToken(token);
        }
    }

    private void registrarFalloDefinitivo(List<PushMessage> mensajes, PushDeliveryResult resultado) {
        String destinos = mensajes.stream()
                .map(PushMessage::getTo)
                .map(this::enmascararToken)
                .collect(Collectors.joining(", "));

        log.error(
                "PUSH_ENVIO_FALLIDO | intentos={} | mensajes={} | destinos=[{}] | error={}",
                resultado.intentosRealizados(),
                mensajes.size(),
                destinos,
                resultado.errorMensaje());
    }

    private String enmascararToken(String token) {
        if (token == null || token.length() <= 12) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}

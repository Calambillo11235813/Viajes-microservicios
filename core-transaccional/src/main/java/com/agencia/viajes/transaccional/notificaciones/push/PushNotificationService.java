package com.agencia.viajes.transaccional.notificaciones.push;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Envío push asíncrono con reintentos ante fallos transitorios del proveedor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushGateway pushGateway;
    private final PushSendResultHandler pushSendResultHandler;

    @Value("${app.push.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.push.retry.initial-delay-ms:2000}")
    private long initialDelayMs;

    @Async("pushTaskExecutor")
    public CompletableFuture<Void> enviarAsync(List<PushMessage> mensajes) {
        if (mensajes.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        PushDeliveryResult resultado = enviarConReintentos(mensajes);
        pushSendResultHandler.procesarResultado(mensajes, resultado);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Reintenta solo errores transitorios (HTTP 5xx, red). Errores definitivos se registran al agotar intentos.
     */
    PushDeliveryResult enviarConReintentos(List<PushMessage> mensajes) {
        int intentos = 0;
        PushTransientException ultimoErrorTransitorio = null;

        while (intentos < maxAttempts) {
            intentos++;
            try {
                PushDeliveryResult resultado = pushGateway.enviar(mensajes);
                if (resultado.exito()) {
                    return PushDeliveryResult.ok(resultado.tokensInvalidos(), intentos);
                }
                log.warn(
                        "Push falló sin reintento (intento {}/{}): {}",
                        intentos,
                        maxAttempts,
                        resultado.errorMensaje());
                return PushDeliveryResult.fallo(resultado.errorMensaje(), intentos);
            } catch (PushTransientException ex) {
                ultimoErrorTransitorio = ex;
                log.warn(
                        "Push transitorio fallido (intento {}/{}): {}",
                        intentos,
                        maxAttempts,
                        ex.getMessage());
                if (intentos < maxAttempts) {
                    esperarBackoff(intentos);
                }
            } catch (Exception ex) {
                log.error("Error inesperado en envío push (intento {}/{}): {}", intentos, maxAttempts, ex.getMessage(), ex);
                return PushDeliveryResult.fallo(ex.getMessage(), intentos);
            }
        }

        String mensaje = ultimoErrorTransitorio != null
                ? ultimoErrorTransitorio.getMessage()
                : "Error transitorio desconocido";
        return PushDeliveryResult.fallo(
                "Agotados " + maxAttempts + " intentos. Último error: " + mensaje,
                intentos);
    }

    private void esperarBackoff(int intento) {
        long delay = initialDelayMs * intento;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PushTransientException("Reintento push interrumpido", ex);
        }
    }
}

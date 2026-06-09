package com.agencia.viajes.transaccional.notificaciones.push;

import java.util.List;

/**
 * Resultado del intento de entrega push ante Expo/FCM.
 */
public record PushDeliveryResult(
        List<String> tokensInvalidos,
        boolean exito,
        String errorMensaje,
        int intentosRealizados) {

    public static PushDeliveryResult ok(List<String> tokensInvalidos, int intentos) {
        return new PushDeliveryResult(tokensInvalidos, true, null, intentos);
    }

    public static PushDeliveryResult fallo(String errorMensaje, int intentos) {
        return new PushDeliveryResult(List.of(), false, errorMensaje, intentos);
    }
}

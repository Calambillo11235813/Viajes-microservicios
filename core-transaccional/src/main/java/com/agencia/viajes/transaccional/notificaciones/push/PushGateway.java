package com.agencia.viajes.transaccional.notificaciones.push;

import java.util.List;

/**
 * Abstracción del proveedor de push (Expo, FCM, etc.).
 */
public interface PushGateway {

    /**
     * Envía una lista de mensajes push.
     *
     * @param mensajes mensajes a enviar.
     * @return resultado de la entrega, incluyendo tokens inválidos si los hubo.
     * @throws PushTransientException si el proveedor falló de forma transitoria (5xx, red).
     */
    PushDeliveryResult enviar(List<PushMessage> mensajes);
}

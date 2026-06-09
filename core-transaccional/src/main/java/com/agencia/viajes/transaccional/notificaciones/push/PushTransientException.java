package com.agencia.viajes.transaccional.notificaciones.push;

/**
 * Error transitorio del proveedor push (5xx, timeout, red) susceptible de reintento.
 */
public class PushTransientException extends RuntimeException {

    public PushTransientException(String message) {
        super(message);
    }

    public PushTransientException(String message, Throwable cause) {
        super(message, cause);
    }
}

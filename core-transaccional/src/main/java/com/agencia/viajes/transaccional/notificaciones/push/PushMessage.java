package com.agencia.viajes.transaccional.notificaciones.push;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload para Expo Push API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushMessage {
    private String to;
    private String title;
    private String body;
    private Map<String, Object> data;
    private String sound;
    private Integer badge;
    /** Canal Android (debe coincidir con setNotificationChannelAsync en la app). */
    private String channelId;
    /** Prioridad Android: default | normal | high */
    private String priority;
}

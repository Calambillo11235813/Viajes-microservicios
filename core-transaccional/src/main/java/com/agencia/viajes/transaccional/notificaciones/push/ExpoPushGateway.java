package com.agencia.viajes.transaccional.notificaciones.push;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente HTTP hacia Expo Push API con detección de errores transitorios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushGateway implements PushGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${app.push.expo-url:https://exp.host/--/api/v2/push/send}")
    private String expoPushUrl;

    @Override
    public PushDeliveryResult enviar(List<PushMessage> mensajes) {
        if (mensajes.isEmpty()) {
            return PushDeliveryResult.ok(List.of(), 0);
        }

        try {
            String responseBody = restClient.post()
                    .uri(expoPushUrl)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(mensajes)
                    .retrieve()
                    .onStatus(status -> status.value() >= 500, (request, response) -> {
                        throw new PushTransientException(
                                "Expo Push API respondió HTTP " + response.getStatusCode().value());
                    })
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return PushDeliveryResult.fallo("Expo Push API devolvió respuesta vacía", 1);
            }

            List<String> tokensInvalidos = extraerTokensInvalidos(responseBody, mensajes);
            return PushDeliveryResult.ok(tokensInvalidos, 1);
        } catch (PushTransientException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().is5xxServerError()) {
                throw new PushTransientException(
                        "Expo Push API respondió HTTP " + ex.getStatusCode().value(), ex);
            }
            String error = "Expo Push API respondió HTTP " + ex.getStatusCode().value()
                    + ": " + ex.getResponseBodyAsString();
            log.error("Error no reintentable enviando push via Expo: {}", error);
            return PushDeliveryResult.fallo(error, 1);
        } catch (RestClientException ex) {
            throw new PushTransientException("Error de red al contactar Expo Push API", ex);
        } catch (Exception ex) {
            if (esErrorRed(ex)) {
                throw new PushTransientException("Error de conexión al contactar Expo Push API", ex);
            }
            log.error("Error inesperado enviando push via Expo: {}", ex.getMessage(), ex);
            return PushDeliveryResult.fallo(ex.getMessage(), 1);
        }
    }

    private boolean esErrorRed(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof IOException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private List<String> extraerTokensInvalidos(String responseBody, List<PushMessage> mensajes) {
        List<String> invalidos = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.get("data");
            if (data == null || !data.isArray()) {
                return invalidos;
            }

            for (int i = 0; i < data.size(); i++) {
                JsonNode item = data.get(i);
                if (item == null) {
                    continue;
                }
                String status = item.path("status").asText("");
                if ("error".equalsIgnoreCase(status)) {
                    String message = item.path("message").asText("");
                    if (message.contains("DeviceNotRegistered")
                            || message.contains("InvalidCredentials")
                            || message.contains("not a registered push notification recipient")) {
                        if (i < mensajes.size()) {
                            invalidos.add(mensajes.get(i).getTo());
                        }
                    } else {
                        log.warn("Expo rechazó mensaje push [{}]: {}", i, message);
                    }
                }
            }
        } catch (Exception ex) {
            log.debug("No se pudo parsear respuesta Expo: {}", ex.getMessage());
        }
        return invalidos;
    }
}

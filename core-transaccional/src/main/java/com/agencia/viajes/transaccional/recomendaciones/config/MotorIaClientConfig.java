package com.agencia.viajes.transaccional.recomendaciones.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración del cliente HTTP usado para comunicarse con el motor IA (Django).
 * Usa {@code Connection: close} para evitar corrupción de peticiones con el
 * servidor de desarrollo de Django (error HTTP {@code Bad request syntax ('7a')}).
 */
@Configuration
public class MotorIaClientConfig {

    /**
     * Expone {@link ObjectMapper} para serializar peticiones JSON al motor IA.
     * Spring Boot 4 con webmvc no lo registra automáticamente como bean.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Construye el {@link RestClient} apuntando al host del motor IA.
     *
     * @param baseUrl host base del motor IA (propiedad {@code motor-ia.base-url}).
     * @param timeoutSeconds timeout de conexión y lectura en segundos.
     * @return cliente REST con conexión no persistente.
     */
    @Bean
    public RestClient motorIaRestClient(
            @Value("${motor-ia.base-url}") String baseUrl,
            @Value("${motor-ia.timeout-seconds:15}") int timeoutSeconds) {
        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeout.toMillis());
        factory.setReadTimeout((int) timeout.toMillis());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("Connection", "close")
                .build();
    }
}

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

    /** Timeout de conexión y de lectura hacia el motor IA. */
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

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
     * @return cliente REST con timeouts de 3 segundos y conexión no persistente.
     */
    @Bean
    public RestClient motorIaRestClient(@Value("${motor-ia.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) TIMEOUT.toMillis());
        factory.setReadTimeout((int) TIMEOUT.toMillis());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("Connection", "close")
                .build();
    }
}

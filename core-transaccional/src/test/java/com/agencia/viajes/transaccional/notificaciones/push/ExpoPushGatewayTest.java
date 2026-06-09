package com.agencia.viajes.transaccional.notificaciones.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class ExpoPushGatewayTest {

    private ExpoPushGateway gateway;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    private RestClient.RequestBodySpec requestBodySpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        requestBodySpec = mock(RestClient.RequestBodySpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).body(any(Object.class));
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        gateway = new ExpoPushGateway(restClient, new ObjectMapper());
        ReflectionTestUtils.setField(gateway, "expoPushUrl", "https://exp.host/--/api/v2/push/send");
    }

    @Test
    void enviar_llamaExpoPushApi() {
        when(responseSpec.body(String.class)).thenReturn("{\"data\":[{\"status\":\"ok\"}]}");

        PushMessage msg = PushMessage.builder()
                .to("ExponentPushToken[abc]")
                .title("Test")
                .body("Body")
                .build();

        PushDeliveryResult resultado = gateway.enviar(List.of(msg));

        verify(requestBodySpec).body(List.of(msg));
        assertThat(resultado.exito()).isTrue();
        assertThat(resultado.tokensInvalidos()).isEmpty();
    }

    @Test
    void enviar_listaVacia_noLlamaApi() {
        PushDeliveryResult resultado = gateway.enviar(List.of());
        assertThat(resultado.exito()).isTrue();
        verify(restClient, never()).post();
    }

    @Test
    void enviar_http5xx_lanzaExcepcionTransitoria() {
        when(responseSpec.body(String.class)).thenThrow(
                new RestClientResponseException("Server Error", 503, "Service Unavailable", null, null, null));

        PushMessage msg = PushMessage.builder()
                .to("ExponentPushToken[abc]")
                .title("Test")
                .body("Body")
                .build();

        assertThatThrownBy(() -> gateway.enviar(List.of(msg)))
                .isInstanceOf(PushTransientException.class)
                .hasMessageContaining("503");
    }

    @Test
    void enviar_http4xx_retornaFalloSinReintento() {
        when(responseSpec.body(String.class)).thenThrow(
                new RestClientResponseException("Bad Request", 400, "Bad Request", null, null, null));

        PushMessage msg = PushMessage.builder()
                .to("ExponentPushToken[abc]")
                .title("Test")
                .body("Body")
                .build();

        PushDeliveryResult resultado = gateway.enviar(List.of(msg));

        assertThat(resultado.exito()).isFalse();
        assertThat(resultado.errorMensaje()).contains("400");
    }
}

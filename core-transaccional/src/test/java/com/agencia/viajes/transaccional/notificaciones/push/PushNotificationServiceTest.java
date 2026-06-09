package com.agencia.viajes.transaccional.notificaciones.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private PushGateway pushGateway;

    @Mock
    private PushSendResultHandler pushSendResultHandler;

    @InjectMocks
    private PushNotificationService pushNotificationService;

    private final PushMessage mensaje = PushMessage.builder()
            .to("ExponentPushToken[abc]")
            .title("Test")
            .body("Body")
            .build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pushNotificationService, "maxAttempts", 3);
        ReflectionTestUtils.setField(pushNotificationService, "initialDelayMs", 1L);
    }

    @Test
    void enviarConReintentos_reintentaErroresTransitorios() {
        when(pushGateway.enviar(any()))
                .thenThrow(new PushTransientException("HTTP 503"))
                .thenThrow(new PushTransientException("HTTP 503"))
                .thenReturn(PushDeliveryResult.ok(List.of(), 1));

        PushDeliveryResult resultado = pushNotificationService.enviarConReintentos(List.of(mensaje));

        assertThat(resultado.exito()).isTrue();
        assertThat(resultado.intentosRealizados()).isEqualTo(3);
        verify(pushGateway, times(3)).enviar(any());
    }

    @Test
    void enviarConReintentos_registraFalloTrasAgotarIntentos() {
        when(pushGateway.enviar(any()))
                .thenThrow(new PushTransientException("HTTP 503"));

        PushDeliveryResult resultado = pushNotificationService.enviarConReintentos(List.of(mensaje));

        assertThat(resultado.exito()).isFalse();
        assertThat(resultado.errorMensaje()).contains("Agotados 3 intentos");
        verify(pushGateway, times(3)).enviar(any());
    }

    @Test
    void enviarConReintentos_noReintentaErroresDefinitivos() {
        when(pushGateway.enviar(any()))
                .thenReturn(PushDeliveryResult.fallo("HTTP 400", 1));

        PushDeliveryResult resultado = pushNotificationService.enviarConReintentos(List.of(mensaje));

        assertThat(resultado.exito()).isFalse();
        assertThat(resultado.errorMensaje()).contains("400");
        verify(pushGateway, times(1)).enviar(any());
    }
}

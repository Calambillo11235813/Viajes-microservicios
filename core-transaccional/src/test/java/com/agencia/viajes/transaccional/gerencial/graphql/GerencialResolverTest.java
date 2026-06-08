package com.agencia.viajes.transaccional.gerencial.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.agencia.viajes.transaccional.config.GraphQLExceptionHandler;
import com.agencia.viajes.transaccional.gerencial.dto.ReglaAsociacionEnriquecida;
import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import com.agencia.viajes.transaccional.usuarios.security.JwtTokenProvider;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.agencia.viajes.transaccional.gerencial.dto.ReglaAsociacionEnriquecida;
import com.agencia.viajes.transaccional.gerencial.service.GerencialService;
import com.agencia.viajes.transaccional.usuarios.security.UsuarioAutenticado;
import graphql.GraphQLContext;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GerencialResolverTest {

    @Mock
    private GerencialService gerencialService;

    @Mock
    private GraphQLContext context;

    @InjectMocks
    private GerencialResolver resolver;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testKpisGeneralesAccesoDenegadoParaUsuarioComun() {
        UsuarioAutenticado usuario = new UsuarioAutenticado();
        usuario.setRol("CLIENTE");
        when(context.get("usuarioAutenticado")).thenReturn(usuario);

        assertThrows(SecurityException.class, () -> {
            resolver.kpisGenerales(context);
        });
    }

    @Test
    void testReglasAsociacionExitosoParaGerente() {
        UsuarioAutenticado usuario = new UsuarioAutenticado();
        usuario.setRol("GERENTE");
        when(context.get("usuarioAutenticado")).thenReturn(usuario);

        when(gerencialService.obtenerReglasAsociacion(1, "lift"))
            .thenReturn(List.of(ReglaAsociacionEnriquecida.builder()
                .interpretacion("Mocked Intepretation")
                .lift(new BigDecimal("1.5"))
                .build()));

        List<ReglaAsociacionEnriquecida> result = resolver.reglasAsociacion(1, "lift", context);
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInterpretacion()).isEqualTo("Mocked Intepretation");
    }
}

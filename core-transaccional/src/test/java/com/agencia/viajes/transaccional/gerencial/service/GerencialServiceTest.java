package com.agencia.viajes.transaccional.gerencial.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.Spy;
import com.agencia.viajes.transaccional.gerencial.dto.KpisGeneralesResponse;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU11SegmentarResponse;
import com.agencia.viajes.transaccional.gerencial.model.DashboardKpiSnapshot;
import com.agencia.viajes.transaccional.gerencial.repository.DashboardKpiSnapshotRepository;
import com.agencia.viajes.transaccional.gerencial.repository.ReglaAsociacionCacheRepository;
import com.agencia.viajes.transaccional.gerencial.repository.UsuarioClusterHistoricoRepository;
import com.agencia.viajes.transaccional.rutas.repository.RutaDestinoRepository;
import com.agencia.viajes.transaccional.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class GerencialServiceTest {

    @Mock private MotorIaGerencialClient motorIaClient;
    @Mock private ClusterCalculadorService clusterCalculadorService;
    @Mock private UsuarioClusterHistoricoRepository usuarioClusterHistoricoRepository;
    @Mock private ReglaAsociacionCacheRepository reglaAsociacionCacheRepository;
    @Mock private DashboardKpiSnapshotRepository dashboardKpiSnapshotRepository;
    @Mock private RutaDestinoRepository rutaDestinoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private ObjectMapper objectMapper;

    @Spy
    private MeterRegistry meterRegistry = new io.micrometer.core.instrument.simple.SimpleMeterRegistry();

    @InjectMocks
    private GerencialService gerencialService;

    @BeforeEach
    void setUp() {
        // MeterRegistry ya no es mock, se usa la implementación Simple
    }

    @Test
    void obtenerKpisGenerales_conDatos() throws Exception {
        DashboardKpiSnapshot snapshot = new DashboardKpiSnapshot();
        snapshot.setFechaSnapshot(LocalDate.now());
        snapshot.setTotalUsuarios(100);
        snapshot.setTotalUsuariosSegmentados(90);
        snapshot.setDistribucionClusters("{}");
        snapshot.setIngresoPorCluster("{}");
        snapshot.setConversionPorCluster("{}");
        
        when(dashboardKpiSnapshotRepository.findTopByOrderByFechaSnapshotDesc()).thenReturn(Optional.of(snapshot));
        when(clusterCalculadorService.inferirEtiquetasClusters()).thenReturn(new HashMap<>());
        
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
            .thenReturn(new HashMap<>());

        KpisGeneralesResponse response = gerencialService.obtenerKpisGenerales();

        assertNotNull(response);
        assertEquals(100, response.getSegmentacion().getTotalUsuarios());
    }

    @Test
    void actualizarClusterDeUsuario_sinGasto_noLlamaIa() {
        Map<String, Object> features = new HashMap<>();
        features.put("total_gastado", BigDecimal.ZERO);
        when(clusterCalculadorService.calcularCaracteristicasUsuario(1)).thenReturn(features);

        gerencialService.actualizarClusterDeUsuario(1);

        verify(motorIaClient, never()).segmentarUsuario(anyMap());
        verify(usuarioClusterHistoricoRepository, never()).save(any());
    }

    @Test
    void actualizarClusterDeUsuario_conGasto_llamaIa() {
        Map<String, Object> features = new HashMap<>();
        features.put("total_gastado", new BigDecimal("150.0"));
        features.put("num_reservas", 2);
        features.put("rutas_distintas", 1);
        features.put("promedio_pasajeros", new BigDecimal("1.5"));
        
        when(clusterCalculadorService.calcularCaracteristicasUsuario(1)).thenReturn(features);
        
        CU11SegmentarResponse iaResponse = new CU11SegmentarResponse();
        iaResponse.setCluster(2);
        when(motorIaClient.segmentarUsuario(features)).thenReturn(iaResponse);

        gerencialService.actualizarClusterDeUsuario(1);

        verify(motorIaClient, times(1)).segmentarUsuario(features);
        verify(usuarioClusterHistoricoRepository, times(1)).save(any());
    }
}

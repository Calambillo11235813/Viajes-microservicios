package com.agencia.viajes.transaccional.gerencial.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.agencia.viajes.transaccional.gerencial.config.MotorIaGerencialProperties;
import com.agencia.viajes.transaccional.gerencial.dto.ia.CU11EstadisticasResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class MotorIaGerencialClientTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private MotorIaGerencialClient client;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("motor-ia.base-url", wireMockServer::baseUrl);
        registry.add("motor-ia.estadisticas-clusters-path", () -> "/api/segmentar-usuario/estadisticas-clusters/");
    }

    @Test
    void obtenerEstadisticasClusters_deserializaCorrectamente() {
        String jsonResponse = """
            {
                "status": "success",
                "n_clusters": 3,
                "clusters": [
                    {
                        "cluster": 0,
                        "centroide": {
                            "total_gastado": 120.5,
                            "num_reservas": 1,
                            "rutas_distintas": 1,
                            "promedio_pasajeros": 1.0
                        }
                    }
                ]
            }
            """;

        stubFor(get(urlEqualTo("/api/segmentar-usuario/estadisticas-clusters/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(jsonResponse)));

        CU11EstadisticasResponse response = client.obtenerEstadisticasClusters();

        assertNotNull(response);
        assertEquals(3, response.getN_clusters());
        assertEquals(1, response.getClusters().size());
        assertEquals(0, response.getClusters().get(0).getCluster());
        assertEquals(120.5, response.getClusters().get(0).getCentroide().getTotal_gastado().doubleValue());
    }
}

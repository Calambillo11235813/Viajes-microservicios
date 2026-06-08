package com.agencia.viajes.transaccional.gerencial.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para los endpoints gerenciales del Motor IA.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "motor-ia")
public class MotorIaGerencialProperties {
    
    private String baseUrl;
    private String reglasAsociacionPath;
    private String estadisticasClustersPath;
    private String segmentarUsuarioPath;

}

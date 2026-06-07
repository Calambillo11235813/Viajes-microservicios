package com.agencia.viajes.transaccional.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Configuración del cliente AWS DynamoDB para escritura de eventos de navegación.
 * Usa {@link DefaultCredentialsProvider} para credenciales locales (~/.aws/credentials)
 * o roles IAM en producción.
 */
@Configuration
@EnableAsync
public class DynamoDbConfig {

    /**
     * Crea el cliente de bajo nivel de DynamoDB.
     *
     * @param region región AWS (propiedad {@code aws.dynamodb.region}).
     * @param endpoint endpoint opcional para DynamoDB Local/LocalStack.
     * @return cliente configurado.
     */
    @Bean
    DynamoDbClient dynamoDbClient(
            @Value("${aws.dynamodb.region}") String region,
            @Value("${aws.dynamodb.endpoint:}") String endpoint) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * Expone el cliente mejorado para mapeo objeto-registro con anotaciones AWS.
     *
     * @param client cliente DynamoDB de bajo nivel.
     * @return cliente mejorado.
     */
    @Bean
    DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }
}

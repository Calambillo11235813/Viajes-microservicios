package com.agencia.viajes.transaccional.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

/**
 * Configuración para envío push asíncrono y cliente HTTP.
 */
@Configuration
@EnableAsync
public class AsyncPushConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean(name = "pushTaskExecutor")
    public Executor pushTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("push-");
        executor.initialize();
        return executor;
    }
}

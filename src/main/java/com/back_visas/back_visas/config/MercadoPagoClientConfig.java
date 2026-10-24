package com.back_visas.back_visas.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoClientConfig {
    @Value("${mercadopago.access.token}")
    private String mercadoPagoToken;
    @Bean
    public RequestInterceptor mercadoPagoInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + mercadoPagoToken);
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}

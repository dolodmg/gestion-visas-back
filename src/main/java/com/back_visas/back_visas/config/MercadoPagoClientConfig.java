package com.back_visas.back_visas.config;

import feign.RequestInterceptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoClientConfig {

    @Value("${mercadopago.access.token}")
    private String mercadoPagoToken;

    @PostConstruct
    public void init() {
        System.out.println("========================================");
        System.out.println("🔑 MERCADOPAGO CONFIG");

        if (mercadoPagoToken == null || mercadoPagoToken.isEmpty()) {
            System.err.println("❌ ERROR: Token de MercadoPago NO configurado");
        } else if (mercadoPagoToken.startsWith("TEST-")) {
            System.err.println("⚠️  SANDBOX MODE: Usando credenciales de prueba");
            System.err.println("   Token: " + mercadoPagoToken.substring(0, 20) + "...");
        } else if (mercadoPagoToken.startsWith("APP_USR-")) {
            System.out.println("✅ PRODUCCIÓN: Credenciales activas");
            System.out.println("   Token: " + mercadoPagoToken.substring(0, 20) + "...");
        } else {
            System.err.println("❓ Token no reconocido: " + mercadoPagoToken.substring(0, 15) + "...");
        }

        System.out.println("========================================");
    }

    @Bean
    public RequestInterceptor mercadoPagoInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + mercadoPagoToken);
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}
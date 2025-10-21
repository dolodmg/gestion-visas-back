package com.back_visas.back_visas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${allowed.origins}") String allowedOrigins) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                "http://localhost*",
                                "http://127.0.0.1*",
                                "https://argenvisa.online*",
                                "https://*.mercadopago.com",
                                "https://*.mercadolibre.com",
                                "https://api.mercadopago.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "X-Total-Count")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
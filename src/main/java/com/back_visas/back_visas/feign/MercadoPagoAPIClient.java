package com.back_visas.back_visas.feign;

import com.back_visas.back_visas.config.MercadoPagoClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "mercadoPagoAPI",
        url = "https://api.mercadopago.com",
        configuration = MercadoPagoClientConfig.class
)
public interface MercadoPagoAPIClient {

    @PostMapping(value = "/checkout/preferences", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> createPreference(@RequestBody Map<String, Object> preference);

    @GetMapping(value = "/checkout/preferences/{id}")
    Map<String, Object> getPreference(@PathVariable("id") String preferenceId);

    @PostMapping(value = "/v1/payments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> processPayment(@RequestBody Map<String, Object> paymentData,
                                       @RequestHeader("X-Idempotency-Key") String idempotencyKey);

    @GetMapping(value = "/v1/payments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getPayment(@PathVariable("id") Long id);
}

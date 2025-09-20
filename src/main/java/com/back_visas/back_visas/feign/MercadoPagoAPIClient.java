package com.back_visas.back_visas.feign;

import com.back_visas.back_visas.config.MercadoPagoClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "mercadoPagoAPI",
        url = "https://api.mercadopago.com",
        configuration = MercadoPagoClientConfig.class)
public interface MercadoPagoAPIClient {
    @PostMapping(value = "/v1/payments",
            consumes = "application/json",
            produces = "application/json")
        Map<String, Object> createPayment(@RequestBody Map<String, Object> paymentRequest,
                                          @RequestHeader("X-Idempotency-Key") String idempotencyKey);

    @GetMapping("/v1/payments/{mpPaymentId}")
    Map<String, Object> getPayment(@PathVariable("mpPaymentId") Long mpPaymentId);
    }

package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.request.CreatePreferenceRequest;
import com.back_visas.back_visas.dto.request.ProcessPaymentRequest;

import java.util.Map;

public interface iMercadoPagoService {
    public Map<String, Object> getPreferenceUrl(String preferenceId) throws Exception;
    public Map<String, Object> createPreference(CreatePreferenceRequest request) throws Exception;

    public Map<String, Object> processPayment(ProcessPaymentRequest request) throws Exception;

    public void handleWebhook(String body, Map<String, String> headers) throws Exception;
    public Map<String, Object> verifyPayment(Long paymentId) throws Exception;
}
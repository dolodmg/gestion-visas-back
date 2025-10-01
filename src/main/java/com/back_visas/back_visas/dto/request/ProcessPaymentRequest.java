package com.back_visas.back_visas.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class ProcessPaymentRequest {
    private Long idOrder;
    private Double amount;
    private String description;
    private Map<String, Object> formData;
    private String preferenceId;
    private String externalReference;
}

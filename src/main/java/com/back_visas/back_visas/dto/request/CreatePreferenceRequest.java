package com.back_visas.back_visas.dto.request;

import lombok.Data;

@Data
public class CreatePreferenceRequest {
    private Long idOrder;
    private Double totalPrice;
    private String description;
    private Integer quantity;
    private String customerMail;
    private String customerName;
    private String externalReference;
}
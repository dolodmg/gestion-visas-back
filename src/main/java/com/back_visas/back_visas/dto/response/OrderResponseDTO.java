package com.back_visas.back_visas.dto.response;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrderResponseDTO {
    private Long idOrder;
    private String serviceName;
    private int quantity;
    private String customerName;
    private String customerLastname;
    private String customerMail;
    private String customerPhone;
    private Double totalPrice;
    private LocalDateTime creationDatetime;
    private String status;
    @Nullable
    private String couponCode;
}

package com.back_visas.back_visas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrderRequestDTO {
    private Long idOrder;
    private Long idService;
    private int quantity;
    private String customerName;
    private String customerLastname;
    private String customerMail;
    private String customerPhone;
    private String couponCode;
}

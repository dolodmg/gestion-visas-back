package com.back_visas.back_visas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CouponRequestDTO {
    private String couponCode;
    private LocalDate expirationDate;
    private Double discount;
}

package com.back_visas.back_visas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCoupon;
    private String couponCode;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private Double discount;
    private boolean active;
}

package com.back_visas.back_visas.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long idOrder;
    private Double totalPrice;
    @ManyToOne
    @JoinColumn(name = "id_service")
    private Service service;
    private int quantity;
    private String customerName;
    private String customerLastname;
    private String customerMail;
    private String customerPhone;
    private LocalDateTime creationDatetime;
    private String paymentId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @ManyToOne
    @JoinColumn(name = "id_coupon")
    @Nullable
    private Coupon coupon;
}

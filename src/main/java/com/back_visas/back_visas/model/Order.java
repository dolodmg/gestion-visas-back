package com.back_visas.back_visas.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long idOrder;
    @Column(name = "external_reference", unique = true)
    private String externalReference;
    private String preferenceId;
    private String description;
    private Double totalPrice;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_service")
    private Service service;
    private int quantity;
    private String customerName;
    private String customerLastname;
    private String customerMail;
    private String customerPhone;
    private LocalDateTime creationDatetime;
    private LocalDateTime updatedAt;
    private String paymentId;
    private boolean includeVideocall = false;
    private Double videocallPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @ManyToOne
    @JoinColumn(name = "id_coupon")
    @Nullable
    private Coupon coupon;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    public Order(String customerMail, String customerName, Double totalPrice, String description) {
        this();
        this.customerMail = customerMail;
        this.customerName = customerName;
        this.totalPrice = totalPrice;
        this.description = description;
    }
    public boolean isPaid() {
        return this.status == OrderStatus.PAID;
    }

    public Payment getLastPayment() {
        return payments.stream()
                .max(Comparator.comparing(Payment::getCreatedAt))
                .orElse(null);
    }

    public Double getPaidAmount() {
        return payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.APPROVED)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public void updateStatus() {
        Double paidAmount = getPaidAmount(); //
        if (paidAmount >= totalPrice) {
            this.status = OrderStatus.PAID;
        } else if (paidAmount > 0.0) {
            this.status = OrderStatus.PARTIALLY_PAID;
        }
        this.updatedAt = LocalDateTime.now();
    }

}

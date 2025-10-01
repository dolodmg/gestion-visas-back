package com.back_visas.back_visas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(name = "mercadopago_payment_id", unique = true)
    private Long mercadoPagoPaymentId;

    //@Column(name = "preference_id")
    private String preferenceId;

    //@Column(name = "amount")
    private Double amount;

    @Enumerated(EnumType.STRING)
    //@Column(name = "status")
    private PaymentStatus status;

    //@Column(name = "status_detail")
    private String statusDetail;

    //@Column(name = "payment_method_id")
    private String paymentMethodId;

    //@Column(name = "payment_type_id")
    private String paymentTypeId;

    //@Column(name = "installments")
    private Integer installments;

    //@Column(name = "transaction_amount")
    private BigDecimal transactionAmount;

    //@Column(name = "net_received_amount")
    private BigDecimal netReceivedAmount;

    //@Column(name = "total_paid_amount")
    private BigDecimal totalPaidAmount;

    //@Column(name = "fee_details", columnDefinition = "TEXT")
    private String feeDetails; // JSON con detalles de comisiones

    //@Column(name = "date_approved")
    private LocalDateTime dateApproved;

    //@Column(name = "date_created")
    private LocalDateTime dateCreated;

    //@Column(name = "date_last_updated")
    private LocalDateTime dateLastUpdated;

    //@Column(name = "created_at")
    private LocalDateTime createdAt;

    //@Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relación con Order
    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "id_order", nullable = false)
    private Order order;

    // Información del pagador
    //@Column(name = "payer_email")
    private String payerEmail;

    //@Column(name = "payer_identification_type")
    private String payerIdentificationType;

    //@Column(name = "payer_identification_number")
    private String payerIdentificationNumber;

    public Payment(Order order, Double amount, String preferenceId) {
        this();
        this.order = order;
        this.amount = amount;
        this.preferenceId = preferenceId;
    }

    public boolean isApproved() {
        return this.status == PaymentStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING || this.status == PaymentStatus.IN_PROCESS;
    }

    public boolean isRejected() {
        return this.status == PaymentStatus.REJECTED || this.status == PaymentStatus.CANCELLED;
    }

    public void updateFromMercadoPagoResponse(Map<String, Object> mpResponse) {
        this.mercadoPagoPaymentId = Long.valueOf(mpResponse.get("id").toString());
        this.status = PaymentStatus.fromString((String) mpResponse.get("status"));
        this.statusDetail = (String) mpResponse.get("status_detail");
        this.paymentMethodId = (String) mpResponse.get("payment_method_id");
        this.paymentTypeId = (String) mpResponse.get("payment_type_id");

        if (mpResponse.get("installments") != null) {
            this.installments = Integer.valueOf(mpResponse.get("installments").toString());
        }

        if (mpResponse.get("transaction_amount") != null) {
            this.transactionAmount = new BigDecimal(mpResponse.get("transaction_amount").toString());
        }

        // Parsear fechas
        if (mpResponse.get("date_approved") != null) {
            this.dateApproved = parseIsoDateTime((String) mpResponse.get("date_approved"));
        }

        if (mpResponse.get("date_created") != null) {
            this.dateCreated = parseIsoDateTime((String) mpResponse.get("date_created"));
        }

        if (mpResponse.get("date_last_updated") != null) {
            this.dateLastUpdated = parseIsoDateTime((String) mpResponse.get("date_last_updated"));
        }

        // Información del pagador
        Map<String, Object> payer = (Map<String, Object>) mpResponse.get("payer");
        if (payer != null) {
            this.payerEmail = (String) payer.get("email");
            Map<String, Object> identification = (Map<String, Object>) payer.get("identification");
            if (identification != null) {
                this.payerIdentificationType = (String) identification.get("type");
                this.payerIdentificationNumber = (String) identification.get("number");
            }
        }

        this.updatedAt = LocalDateTime.now();
    }

    private LocalDateTime parseIsoDateTime(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
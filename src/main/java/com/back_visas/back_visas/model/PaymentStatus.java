package com.back_visas.back_visas.model;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("pending"),
    APPROVED("approved"),
    AUTHORIZED("authorized"),
    IN_PROCESS("in_process"),
    IN_MEDIATION("in_mediation"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    REFUNDED("refunded"),
    CHARGED_BACK("charged_back");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public static PaymentStatus fromString(String value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}

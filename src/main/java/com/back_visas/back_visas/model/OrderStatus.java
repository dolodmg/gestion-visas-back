package com.back_visas.back_visas.model;

public enum OrderStatus {
    PENDING("pending"),
    PAID("paid"),
    PARTIALLY_PAID("partially_paid"),
    CANCELLED("cancelled"),
    EXPIRED("expired");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderStatus fromString(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PENDING;
    }
}


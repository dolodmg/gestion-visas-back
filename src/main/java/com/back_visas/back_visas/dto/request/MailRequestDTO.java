package com.back_visas.back_visas.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class MailRequestDTO {
        private String to;
        private String nameTo;
        private String templateId;
        private String firstName;
        private String serviceName;
        private String idOrder;
        private String creationDatetime;
        private String totalPrice;
        private String couponCode;
        private int quantity;
}

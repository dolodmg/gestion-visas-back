package com.back_visas.back_visas.dto.request;

import lombok.Data;

@Data
public class ContactRequestDTO {
    private String name;
    private String email;
    private String message;
}

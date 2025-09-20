package com.back_visas.back_visas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Service {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idService;
    private String serviceName;
    private Double pricePerPerson;
    private boolean allowsVariableQuantity = false;
    private int fixedQuantity = 1;
}

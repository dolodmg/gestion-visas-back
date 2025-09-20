package com.back_visas.back_visas.dto.request;

import com.back_visas.back_visas.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrderStatusRequestDTO {
    private OrderStatus status;
}

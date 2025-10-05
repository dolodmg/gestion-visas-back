package com.back_visas.back_visas.mapper;

import com.back_visas.back_visas.dto.request.OrderRequestDTO;
import com.back_visas.back_visas.dto.response.OrderResponseDTO;
import com.back_visas.back_visas.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    // de requestDTO a entidad
    @Mapping(target = "idOrder", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "creationDatetime", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "coupon", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(source = "requestedQuantity", target = "quantity")
    Order toEntity(OrderRequestDTO dto);

    // de entidad a responseDTO
    @Mapping(source = "service.serviceName", target = "serviceName")
    @Mapping(source = "service.idService", target = "idService")
    @Mapping(source = "coupon.couponCode", target = "couponCode")
    OrderResponseDTO toDto(Order order);
}

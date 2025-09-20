package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.request.OrderRequestDTO;
import com.back_visas.back_visas.dto.response.OrderResponseDTO;
import com.back_visas.back_visas.model.Order;
import com.back_visas.back_visas.model.OrderStatus;

import java.util.List;
import java.util.Map;

public interface iOrderService {
    public List<OrderResponseDTO> getOrders();
    public OrderResponseDTO getOrder(Long idOrder);
    public OrderResponseDTO createOrder(OrderRequestDTO dto);
    public Order changeStatus(Long idOrder, OrderStatus status);
    void handleMercadoPagoWebhook(Map<String, Object> payload);
}

package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.request.OrderRequestDTO;
import com.back_visas.back_visas.dto.response.OrderResponseDTO;
import com.back_visas.back_visas.exception.InvalidQuantityException;
import com.back_visas.back_visas.mapper.OrderMapper;
import com.back_visas.back_visas.model.Order;
import com.back_visas.back_visas.model.OrderStatus;
import com.back_visas.back_visas.repository.CouponRepository;
import com.back_visas.back_visas.repository.OrderRepository;
import com.back_visas.back_visas.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderService implements iOrderService {
    private final OrderRepository orderRepository;
    private final ServiceRepository serviceRepository;
    private final CouponRepository couponRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, ServiceRepository serviceRepository, CouponRepository couponRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.serviceRepository = serviceRepository;
        this.couponRepository = couponRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public List<OrderResponseDTO> getOrders() {
        return null;
    }

    @Override
    public OrderResponseDTO getOrder(Long idOrder) {
        return null;
    }

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO dto, int requestedQuantity) {
        Order order = orderMapper.toEntity(dto);
        com.back_visas.back_visas.model.Service service = serviceRepository.findById(dto.getIdService())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        order.setService(service);

        // VALIDACIÓN DE QUANTITY
        if (!service.isAllowsVariableQuantity() && requestedQuantity != 1) {
            throw new InvalidQuantityException("El servicio " + service.getServiceName() + " solo permite cantidad de 1");
        }

        // Establecer la quantity validada
        order.setQuantity(requestedQuantity);

        if (dto.getCouponCode() != null) {
            couponRepository.findByCouponCodeAndExpirationDateAfter(
                    dto.getCouponCode(), LocalDate.now()
            ).ifPresent(order::setCoupon);
        }

        Double total = service.getPricePerPerson() * requestedQuantity;
        if (order.getCoupon() != null) {
            total = total * (1 - order.getCoupon().getDiscount() / 100.0);
        }

        order.setTotalPrice(total);
        order.setCreationDatetime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDIENTE);
        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    public Order changeStatus(Long idOrder, OrderStatus status) {
        return null;
    }

    @Override
    public void handleMercadoPagoWebhook(Map<String, Object> payload) {

    }
}

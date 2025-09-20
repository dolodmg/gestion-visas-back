package com.back_visas.back_visas.service;

import com.back_visas.back_visas.dto.request.OrderRequestDTO;
import com.back_visas.back_visas.dto.response.OrderResponseDTO;
import com.back_visas.back_visas.exception.InvalidQuantityException;
import com.back_visas.back_visas.feign.MercadoPagoAPIClient;
import com.back_visas.back_visas.mapper.OrderMapper;
import com.back_visas.back_visas.model.Order;
import com.back_visas.back_visas.model.OrderStatus;
import com.back_visas.back_visas.repository.CouponRepository;
import com.back_visas.back_visas.repository.OrderRepository;
import com.back_visas.back_visas.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService implements iOrderService {
    private final OrderRepository orderRepository;
    private final ServiceRepository serviceRepository;
    private final CouponRepository couponRepository;
    private final OrderMapper orderMapper;

    private final MercadoPagoAPIClient mercadoPagoAPIClient;

    public OrderService(OrderRepository orderRepository, ServiceRepository serviceRepository, CouponRepository couponRepository, OrderMapper orderMapper, MercadoPagoAPIClient mercadoPagoAPIClient) {
        this.orderRepository = orderRepository;
        this.serviceRepository = serviceRepository;
        this.couponRepository = couponRepository;
        this.orderMapper = orderMapper;
        this.mercadoPagoAPIClient = mercadoPagoAPIClient;
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
        Order order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        // Validaciones de transiciones de estado
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        return orderRepository.save(order);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.APROBADA && newStatus == OrderStatus.PENDIENTE) {
            throw new IllegalStateException("No se puede cambiar de APROBADA a PENDIENTE");
        }
    }
    @Override
    public void handleMercadoPagoWebhook(Map<String, Object> payload) {
        try {
            // ✅ Validación del payload (igual que tu código)
            if (payload == null) {
                System.err.println("Webhook payload es null");
                return;
            }

            String type = (String) payload.get("type");
            if (!"payment".equals(type)) {
                System.out.println("Webhook ignorado - tipo: " + type);
                return;
            }

            // ✅ Extraer ID (igual que tu código)
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null) return;

            Object idObj = data.get("id");
            if (idObj == null) return;

            Long mpPaymentId;
            try {
                if (idObj instanceof Integer) {
                    mpPaymentId = ((Integer) idObj).longValue();
                } else if (idObj instanceof Long) {
                    mpPaymentId = (Long) idObj;
                } else {
                    mpPaymentId = Long.valueOf(idObj.toString());
                }
            } catch (NumberFormatException e) {
                System.err.println("Error convirtiendo ID: " + idObj);
                return;
            }

            // DIFERENCIA: Buscar Order por paymentId en lugar de Payment
            Optional<Order> orderOpt = orderRepository.findByPaymentId(mpPaymentId.toString());
            if (orderOpt.isEmpty()) {
                System.err.println("No se encontró orden con MP Payment ID: " + mpPaymentId);
                return;
            }

            Order order = orderOpt.get();
            OrderStatus oldStatus = order.getStatus();

            // Consultar el estado en MercadoPago
            Map<String, Object> mpPayment = getPaymentWithRetry(mpPaymentId);
            if (mpPayment == null) return;

            String mpStatus = (String) mpPayment.get("status");

            // Mapeo de estados (adaptado a tus OrderStatus)
            OrderStatus newStatus = mapMercadoPagoStatusToOrderStatus(mpStatus);

            // Solo actualizar si cambió
            if (oldStatus != newStatus) {
                order.setStatus(newStatus);
                orderRepository.save(order);
                System.out.println("Orden " + order.getIdOrder() + " actualizada: " + oldStatus + " -> " + newStatus);
            }

        } catch (Exception e) {
            System.err.println("Error en webhook: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Object> getPaymentWithRetry(Long paymentId) {
        int maxRetries = 3;
        int currentRetry = 0;

        while (currentRetry < maxRetries) {
            try {
                // Llamada a tu cliente de MercadoPago
                return mercadoPagoAPIClient.getPayment(paymentId);

            } catch (Exception e) {
                currentRetry++;
                System.err.println("Retry " + currentRetry + "/" + maxRetries + " - Error: " + e.getMessage());

                if (currentRetry >= maxRetries) {
                    System.err.println("Max retries alcanzado para payment ID: " + paymentId);
                    return null;
                }

                // Esperar antes del siguiente retry
                try {
                    Thread.sleep(1000 * currentRetry); // 1s, 2s, 3s
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }
}

    private OrderStatus mapMercadoPagoStatusToOrderStatus(String mpStatus) {
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> OrderStatus.APROBADA;
            case "rejected" -> OrderStatus.RECHAZADA;
            default -> OrderStatus.PENDIENTE;
        };
    }
}

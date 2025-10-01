package com.back_visas.back_visas.controller;

import com.back_visas.back_visas.dto.request.OrderRequestDTO;
import com.back_visas.back_visas.dto.request.OrderStatusRequestDTO;
import com.back_visas.back_visas.dto.response.OrderResponseDTO;
import com.back_visas.back_visas.model.Order;
import com.back_visas.back_visas.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getOrders() {
        List<OrderResponseDTO> orders = orderService.getOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{idOrder}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long idOrder) {
        OrderResponseDTO order = orderService.getOrder(idOrder);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO dto) {
        OrderResponseDTO order = orderService.createOrder(dto);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/edit/{idOrder}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long idOrder, @RequestBody OrderRequestDTO dto) {
        OrderResponseDTO order = orderService.updateOrder(idOrder, dto);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{idOrder}/status")
    public ResponseEntity<Order> changeStatus(@PathVariable Long idOrder, @RequestBody OrderStatusRequestDTO status) {
        Order order = orderService.changeStatus(idOrder, status.getStatus());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            orderService.handleMercadoPagoWebhook(payload);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error en webhook: " + e.getMessage());
            return ResponseEntity.ok().build();
        }
    }
}

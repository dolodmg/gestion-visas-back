package com.back_visas.back_visas.repository;

import com.back_visas.back_visas.model.Order;
import com.back_visas.back_visas.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPaymentId(String paymentId);
    Optional<Order> findByExternalReference(String externalReference);
    List<Order> findByCustomerMail(String customerMail);
    List<Order> findByStatus(OrderStatus status);
}

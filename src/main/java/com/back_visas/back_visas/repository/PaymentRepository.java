package com.back_visas.back_visas.repository;

import com.back_visas.back_visas.model.Payment;
import com.back_visas.back_visas.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMercadoPagoPaymentId(Long mercadoPagoPaymentId);
    Optional<Payment> findByPreferenceId(String preferenceId);
}

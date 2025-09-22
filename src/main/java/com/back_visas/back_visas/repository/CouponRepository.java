package com.back_visas.back_visas.repository;

import com.back_visas.back_visas.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;


@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponCodeAndExpirationDateAfter(String couponCode, LocalDate date);
    Optional<Coupon> findCouponByCouponCodeIgnoreCase(String couponCode);
}

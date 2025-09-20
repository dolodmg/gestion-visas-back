package com.back_visas.back_visas.controller;

import com.back_visas.back_visas.model.Coupon;
import com.back_visas.back_visas.service.CouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getCoupons() {
        List<Coupon> coupons = couponService.getCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{idCoupon}")
    public ResponseEntity<Coupon> getCoupon(@PathVariable Long idCoupon) {
        Coupon coupon = couponService.getCoupon(idCoupon);
        return ResponseEntity.ok(coupon);
    }

    @PostMapping
    public void createCoupon(@RequestBody Coupon coupon) {
        couponService.createCoupon(coupon);
    }

    @DeleteMapping("/{idCoupon}")
    public void deleteCoupon(@PathVariable Long idCoupon) {
        couponService.deleteCoupon(idCoupon);
    }

    @PatchMapping("/{idCoupon}")
    public void toggleActive(@PathVariable Long idCoupon) {
        couponService.toggleActive(idCoupon);
    }
}

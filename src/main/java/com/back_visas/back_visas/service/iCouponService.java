package com.back_visas.back_visas.service;

import com.back_visas.back_visas.model.Coupon;

import java.util.List;

public interface iCouponService {
    public Coupon getCoupon(Long idCoupon);
    public List<Coupon> getCoupons();
    public void createCoupon(Coupon coupon);
    public void deleteCoupon(Long idCoupon);
    public void toggleActive(Long idCoupon);
    public Coupon getCouponByCode(String couponCode);
}

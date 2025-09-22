package com.back_visas.back_visas.service;

import com.back_visas.back_visas.model.Coupon;
import com.back_visas.back_visas.repository.CouponRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CouponService implements iCouponService {
    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public Coupon getCoupon(Long idCoupon) {
        return couponRepository.findById(idCoupon)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró un cupón con ID " + idCoupon));
    }

    @Override
    public List<Coupon> getCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public void createCoupon(Coupon coupon) {
        couponRepository.save(coupon);
    }

    @Override
    public void deleteCoupon(Long idCoupon) {
        couponRepository.deleteById(idCoupon);
    }

    @Override
    public void toggleActive(Long idCoupon) {
        Coupon coupon = couponRepository.findById(idCoupon)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró un cupón con ID " + idCoupon));
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
    }

    @Override
    public Coupon getCouponByCode(String couponCode) {
        return couponRepository.findCouponByCouponCodeIgnoreCase(couponCode)
                .orElse(null);
    }

}

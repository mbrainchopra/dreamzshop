package org.example.dreamzshop.service;

import org.example.dreamzshop.dto.CouponValidationResult;
import org.example.dreamzshop.entity.Coupon;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CouponService {

    Page<Coupon> getAllCoupons(
            Pageable pageable
    );

    Coupon getCoupon(
            Long id
    );

    Coupon getCouponByCode(
            String code
    );

    Coupon saveCoupon(
            Coupon coupon
    );

    Coupon updateCoupon(
            Long id,
            Coupon coupon
    );

    void deleteCoupon(
            Long id
    );

    void toggleCoupon(
            Long id
    );

    CouponValidationResult validateCoupon(
            String email,
            String code,
            BigDecimal orderAmount
    );

    BigDecimal calculateDiscount(
            Coupon coupon,
            BigDecimal orderAmount
    );

    void recordCouponUsage(
            Coupon coupon,
            User user,
            Order order,
            BigDecimal discountAmount
    );
}
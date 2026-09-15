package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponUsageRepository
        extends JpaRepository<CouponUsage, Long> {

    long countByCouponId(
            Long couponId
    );

    long countByCouponIdAndUserId(
            Long couponId,
            Long userId
    );

    boolean existsByCouponIdAndOrderId(
            Long couponId,
            Long orderId
    );
}
package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository
        extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(
            String code
    );

    boolean existsByCodeIgnoreCase(
            String code
    );

    Page<Coupon> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    long countByEnabledTrue();

    long countByEndDateBefore(
            LocalDateTime dateTime
    );
}
package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Refund;
import org.example.dreamzshop.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository
        extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundNumber(
            String refundNumber
    );

    boolean existsByRefundNumber(
            String refundNumber
    );

    Optional<Refund> findByIdAndUserId(
            Long id,
            Long userId
    );

    Optional<Refund> findByOrderId(
            Long orderId
    );

    Optional<Refund> findByReturnRequestId(
            Long returnRequestId
    );

    boolean existsByOrderId(
            Long orderId
    );

    boolean existsByReturnRequestId(
            Long returnRequestId
    );

    Page<Refund> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Page<Refund> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    Page<Refund> findByStatusOrderByCreatedAtDesc(
            RefundStatus status,
            Pageable pageable
    );

    long countByStatus(
            RefundStatus status
    );
}
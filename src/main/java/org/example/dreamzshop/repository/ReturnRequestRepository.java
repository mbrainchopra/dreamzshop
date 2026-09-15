package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.ReturnRequest;
import org.example.dreamzshop.enums.ReturnRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReturnRequestRepository
        extends JpaRepository<ReturnRequest, Long> {

    Page<ReturnRequest> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Page<ReturnRequest> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    Page<ReturnRequest> findByStatusOrderByCreatedAtDesc(
            ReturnRequestStatus status,
            Pageable pageable
    );

    Optional<ReturnRequest> findByIdAndUserId(
            Long id,
            Long userId
    );

    Optional<ReturnRequest> findByOrderId(
            Long orderId
    );

    boolean existsByOrderId(
            Long orderId
    );

    boolean existsByOrderIdAndUserId(
            Long orderId,
            Long userId
    );

    long countByStatus(
            ReturnRequestStatus status
    );
}
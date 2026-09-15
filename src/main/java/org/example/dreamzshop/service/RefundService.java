package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Refund;
import org.example.dreamzshop.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface RefundService {

    /*
     * Create a refund for an approved/eligible return request.
     */
    Refund createRefund(
            Long returnRequestId,
            BigDecimal refundAmount,
            String refundMethod,
            String remarks
    );

    /*
     * Customer refund history.
     */
    Page<Refund> getCustomerRefunds(
            String email,
            Pageable pageable
    );

    /*
     * Get customer's own refund.
     */
    Refund getCustomerRefund(
            String email,
            Long refundId
    );

    /*
     * Admin - get all refunds.
     */
    Page<Refund> getAllRefunds(
            Pageable pageable
    );

    /*
     * Admin - filter refunds by status.
     */
    Page<Refund> getRefundsByStatus(
            RefundStatus status,
            Pageable pageable
    );

    /*
     * Admin - get refund details.
     */
    Refund getRefund(
            Long refundId
    );

    /*
     * Admin - update refund status.
     */
    void updateRefundStatus(
            Long refundId,
            RefundStatus newStatus,
            String transactionReference,
            String remarks
    );

    /*
     * Check whether a refund already exists
     * for a return request.
     */
    boolean existsForReturnRequest(
            Long returnRequestId
    );
}
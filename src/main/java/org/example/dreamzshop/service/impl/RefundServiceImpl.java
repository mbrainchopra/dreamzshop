package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.Refund;
import org.example.dreamzshop.entity.ReturnRequest;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.PaymentStatus;
import org.example.dreamzshop.enums.RefundStatus;
import org.example.dreamzshop.enums.ReturnRequestStatus;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.OrderRepository;
import org.example.dreamzshop.repository.RefundRepository;
import org.example.dreamzshop.repository.ReturnRequestRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.RefundService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public Refund createRefund(
            Long returnRequestId,
            BigDecimal refundAmount,
            String refundMethod,
            String remarks
    ) {

        if (returnRequestId == null) {
            throw new IllegalArgumentException(
                    "Return request is required."
            );
        }

        if (refundAmount == null ||
                refundAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Refund amount must be greater than zero."
            );
        }

        ReturnRequest returnRequest =
                returnRequestRepository.findById(returnRequestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Return request not found."
                                )
                        );

        if (refundRepository.existsByReturnRequestId(
                returnRequestId)) {

            throw new IllegalArgumentException(
                    "Refund already exists for this return request."
            );
        }

        /*
         * Refund can be created only after the returned
         * product has reached the refund stage.
         */
        if (returnRequest.getStatus()
                != ReturnRequestStatus.REFUND_INITIATED) {

            throw new IllegalArgumentException(
                    "Refund can be created only when the return request is in REFUND_INITIATED status."
            );
        }

        Order order = returnRequest.getOrder();

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order associated with the return request was not found."
            );
        }

        User user = returnRequest.getUser();

        if (user == null) {
            throw new IllegalArgumentException(
                    "Customer associated with the refund was not found."
            );
        }

        if (refundAmount.compareTo(
                order.getGrandTotal()
        ) > 0) {

            throw new IllegalArgumentException(
                    "Refund amount cannot exceed the order total."
            );
        }

        String normalizedMethod =
                refundMethod == null ||
                        refundMethod.trim().isEmpty()
                        ? "COD_BANK_TRANSFER"
                        : refundMethod.trim()
                        .toUpperCase();

        Refund refund = Refund.builder()
                .refundNumber(generateRefundNumber())
                .user(user)
                .order(order)
                .returnRequest(returnRequest)
                .refundAmount(refundAmount)
                .status(RefundStatus.PENDING)
                .refundMethod(normalizedMethod)
                .remarks(
                        remarks == null
                                ? null
                                : remarks.trim()
                )
                .build();

        /*
         * The order is now waiting for refund processing.
         */
        order.setOrderStatus(
                OrderStatus.REFUND_REQUESTED
        );

        order.setPaymentStatus(
                PaymentStatus.PENDING
        );

        orderRepository.save(order);

        return refundRepository.save(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Refund> getCustomerRefunds(
            String email,
            Pageable pageable
    ) {

        User user = getCustomer(email);

        return refundRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Refund getCustomerRefund(
            String email,
            Long refundId
    ) {

        User user = getCustomer(email);

        return refundRepository
                .findByIdAndUserId(
                        refundId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Refund not found."
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Refund> getAllRefunds(
            Pageable pageable
    ) {

        return refundRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Refund> getRefundsByStatus(
            RefundStatus status,
            Pageable pageable
    ) {

        if (status == null) {
            return getAllRefunds(pageable);
        }

        return refundRepository
                .findByStatusOrderByCreatedAtDesc(
                        status,
                        pageable
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Refund getRefund(
            Long refundId
    ) {

        return refundRepository
                .findById(refundId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Refund not found."
                        )
                );
    }

    @Override
    public void updateRefundStatus(
            Long refundId,
            RefundStatus newStatus,
            String transactionReference,
            String remarks
    ) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Refund status is required."
            );
        }

        Refund refund = getRefund(refundId);

        RefundStatus currentStatus =
                refund.getStatus();

        if (currentStatus == newStatus) {

            updateRefundDetails(
                    refund,
                    transactionReference,
                    remarks
            );

            refundRepository.save(refund);
            return;
        }

        validateStatusTransition(
                currentStatus,
                newStatus
        );

        refund.setStatus(newStatus);

        if (transactionReference != null &&
                !transactionReference.trim().isEmpty()) {

            refund.setTransactionReference(
                    transactionReference.trim()
            );
        }

        if (remarks != null) {
            refund.setRemarks(
                    remarks.trim()
            );
        }

        /*
         * When refund is completed:
         *
         * Return request → COMPLETED
         * Order → REFUNDED
         * Payment → REFUNDED
         */
        if (newStatus == RefundStatus.COMPLETED) {

            refund.setCompletedAt(
                    LocalDateTime.now()
            );

            ReturnRequest returnRequest =
                    refund.getReturnRequest();

            if (returnRequest != null) {

                returnRequest.setStatus(
                        ReturnRequestStatus.COMPLETED
                );

                returnRequest.setUpdatedAt(
                        LocalDateTime.now()
                );

                returnRequestRepository.save(
                        returnRequest
                );
            }

            Order order = refund.getOrder();

            if (order != null) {

                order.setOrderStatus(
                        OrderStatus.REFUNDED
                );

                order.setPaymentStatus(
                        PaymentStatus.REFUNDED
                );

                orderRepository.save(order);
            }
        }

        /*
         * Failed refund keeps the order in refund-requested
         * state so admin can retry/process it.
         */
        if (newStatus == RefundStatus.FAILED) {

            Order order = refund.getOrder();

            if (order != null) {

                order.setOrderStatus(
                        OrderStatus.REFUND_REQUESTED
                );

                order.setPaymentStatus(
                        PaymentStatus.PENDING
                );

                orderRepository.save(order);
            }
        }

        /*
         * Cancelled refund returns the order to the
         * previous delivered state.
         */
        if (newStatus == RefundStatus.CANCELLED) {

            Order order = refund.getOrder();

            if (order != null) {

                order.setOrderStatus(
                        OrderStatus.DELIVERED
                );

                order.setPaymentStatus(
                        PaymentStatus.SUCCESS
                );

                orderRepository.save(order);
            }
        }

        refundRepository.save(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsForReturnRequest(
            Long returnRequestId
    ) {

        if (returnRequestId == null) {
            return false;
        }

        return refundRepository
                .existsByReturnRequestId(
                        returnRequestId
                );
    }

    /*
     * Validate refund lifecycle.
     */
    private void validateStatusTransition(
            RefundStatus currentStatus,
            RefundStatus newStatus
    ) {

        boolean valid = switch (currentStatus) {

            case PENDING ->
                    newStatus == RefundStatus.INITIATED ||
                            newStatus == RefundStatus.CANCELLED;

            case INITIATED ->
                    newStatus == RefundStatus.PROCESSING ||
                            newStatus == RefundStatus.FAILED ||
                            newStatus == RefundStatus.CANCELLED;

            case PROCESSING ->
                    newStatus == RefundStatus.COMPLETED ||
                            newStatus == RefundStatus.FAILED;

            case FAILED ->
                    newStatus == RefundStatus.INITIATED ||
                            newStatus == RefundStatus.CANCELLED;

            case COMPLETED,
                 CANCELLED ->
                    false;
        };

        if (!valid) {

            throw new IllegalArgumentException(
                    "Invalid refund status transition: "
                            + currentStatus
                            + " → "
                            + newStatus
            );
        }
    }

    /*
     * Update optional refund information.
     */
    private void updateRefundDetails(
            Refund refund,
            String transactionReference,
            String remarks
    ) {

        if (transactionReference != null &&
                !transactionReference.trim().isEmpty()) {

            refund.setTransactionReference(
                    transactionReference.trim()
            );
        }

        if (remarks != null) {
            refund.setRemarks(
                    remarks.trim()
            );
        }
    }

    /*
     * Get customer and verify role.
     */
    private User getCustomer(
            String email
    ) {

        if (email == null ||
                email.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Customer email is required."
            );
        }

        User user =
                userRepository.findByEmail(
                        email.trim()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found."
                        )
                );

        if (user.getRole() != Role.CUSTOMER) {

            throw new IllegalArgumentException(
                    "Only customers can access refunds."
            );
        }

        if (!user.isEnabled()) {

            throw new IllegalArgumentException(
                    "Customer account is disabled."
            );
        }

        return user;
    }

    /*
     * Generate unique refund reference.
     *
     * Example:
     * REF-20260906-A1B2C3D4
     */
    private String generateRefundNumber() {

        String refundNumber;

        do {

            refundNumber =
                    "REF-"
                            + LocalDateTime.now()
                            .toLocalDate()
                            .toString()
                            .replace("-", "")
                            + "-"
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

        } while (
                refundRepository.existsByRefundNumber(
                        refundNumber
                )
        );

        return refundNumber;
    }
}
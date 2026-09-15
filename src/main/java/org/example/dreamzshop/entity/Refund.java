package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.dreamzshop.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "refunds",
        indexes = {
                @Index(
                        name = "idx_refund_order",
                        columnList = "order_id"
                ),
                @Index(
                        name = "idx_refund_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_refund_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Unique refund reference number.
     */
    @Column(
            name = "refund_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String refundNumber;

    /*
     * Customer receiving the refund.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /*
     * Original order.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    /*
     * Related return request.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "return_request_id",
            unique = true
    )
    private ReturnRequest returnRequest;

    /*
     * Amount to be refunded.
     */
    @DecimalMin(value = "0.00")
    @Column(
            name = "refund_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal refundAmount;

    /*
     * Refund status.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private RefundStatus status =
            RefundStatus.PENDING;

    /*
     * Refund method.
     *
     * Examples:
     * COD_BANK_TRANSFER
     * RAZORPAY
     * MANUAL
     */
    @Column(
            name = "refund_method",
            length = 50
    )
    private String refundMethod;

    /*
     * External payment/refund reference.
     *
     * Razorpay refund ID can be stored here later.
     */
    @Column(
            name = "transaction_reference",
            length = 100
    )
    private String transactionReference;

    /*
     * Customer-facing/admin notes.
     */
    @Size(max = 1000)
    @Column(
            name = "remarks",
            length = 1000
    )
    private String remarks;

    /*
     * Refund creation time.
     */
    @Column(
            name = "created_at",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();

    /*
     * Last update time.
     */
    @Column(
            name = "updated_at",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime updatedAt =
            LocalDateTime.now();

    /*
     * Actual refund completion time.
     */
    @Column(
            name = "completed_at"
    )
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = RefundStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }

    /*
     * Mark refund as completed.
     */
    public void markCompleted() {

        this.status =
                RefundStatus.COMPLETED;

        this.completedAt =
                LocalDateTime.now();

        this.updatedAt =
                LocalDateTime.now();
    }
}
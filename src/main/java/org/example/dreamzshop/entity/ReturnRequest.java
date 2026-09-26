package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import org.example.dreamzshop.enums.ReturnRequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "return_requests",
        indexes = {
                @Index(name = "idx_return_order", columnList = "order_id"),
                @Index(name = "idx_return_user", columnList = "user_id"),
                @Index(name = "idx_return_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Customer who requested the return.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /*
     * Original delivered order.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    /*
     * Specific item being returned.
     *
     * Return requests are product/item-level, so the database requires
     * order_item_id for every return request.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_item_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_return_request_order_item")
    )
    private OrderItem orderItem;

    /*
     * Reason selected by customer.
     */
    @NotBlank
    @Size(max = 500)
    @Column(
            name = "reason",
            nullable = false,
            length = 500
    )
    private String reason;

    /*
     * Additional explanation from customer.
     */
    @Size(max = 1000)
    @Column(
            name = "description",
            length = 1000
    )
    private String description;

    /*
     * Current return status.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 40
    )
    @Builder.Default
    private ReturnRequestStatus status =
            ReturnRequestStatus.REQUESTED;

    /*
     * Admin remarks.
     */
    @Size(max = 1000)
    @Column(
            name = "admin_remarks",
            length = 1000
    )
    private String adminRemarks;

    /*
     * Return request creation time.
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
            status =
                    ReturnRequestStatus.REQUESTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}
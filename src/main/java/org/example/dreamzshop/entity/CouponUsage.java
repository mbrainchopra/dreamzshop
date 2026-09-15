package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon_usages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupon_usage_order",
                        columnNames = {"coupon_id", "order_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_coupon_usage_coupon",
                        columnList = "coupon_id"
                ),
                @Index(
                        name = "idx_coupon_usage_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_coupon_usage_order",
                        columnList = "order_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "coupon_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_coupon_usage_coupon"
            )
    )
    private Coupon coupon;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_coupon_usage_user"
            )
    )
    private User user;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_coupon_usage_order"
            )
    )
    private Order order;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountAmount;

    @Column(
            name = "used_at",
            nullable = false,
            updatable = false
    )
    @Builder.Default
    private LocalDateTime usedAt =
            LocalDateTime.now();

    @PrePersist
    protected void onCreate() {

        if (usedAt == null) {
            usedAt = LocalDateTime.now();
        }
    }
}
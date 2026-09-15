package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.dreamzshop.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupons",
        indexes = {
                @Index(name = "idx_coupon_code", columnList = "code"),
                @Index(name = "idx_coupon_enabled", columnList = "enabled"),
                @Index(name = "idx_coupon_dates", columnList = "start_date,end_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Size(
            min = 3,
            max = 50,
            message = "Coupon code must be between 3 and 50 characters"
    )
    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "discount_type",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private DiscountType discountType =
            DiscountType.PERCENTAGE;

    @NotNull(message = "Discount value is required")
    @DecimalMin(
            value = "0.01",
            message = "Discount value must be greater than zero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Invalid discount value"
    )
    @Column(
            name = "discount_value",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal discountValue;

    @DecimalMin(
            value = "0.00",
            message = "Minimum order amount cannot be negative"
    )
    @Digits(
            integer = 12,
            fraction = 2
    )
    @Column(
            name = "minimum_order_amount",
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal minimumOrderAmount =
            BigDecimal.ZERO;

    @DecimalMin(
            value = "0.00",
            message = "Maximum discount cannot be negative"
    )
    @Digits(
            integer = 12,
            fraction = 2
    )
    @Column(
            name = "maximum_discount_amount",
            precision = 12,
            scale = 2
    )
    private BigDecimal maximumDiscountAmount;

    @Column(
            name = "usage_limit"
    )
    private Integer usageLimit;

    @Column(
            name = "used_count",
            nullable = false
    )
    @Builder.Default
    private Integer usedCount = 0;

    @Column(
            name = "per_customer_limit"
    )
    private Integer perCustomerLimit;

    @Column(
            name = "first_order_only",
            nullable = false
    )
    @Builder.Default
    private boolean firstOrderOnly = false;

    @NotNull(message = "Start date is required")
    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDateTime endDate;

    @Column(
            nullable = false
    )
    @Builder.Default
    private boolean enabled = true;

    @Column(
            nullable = false,
            updatable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();

    private LocalDateTime updatedAt;

    public boolean isCurrentlyActive() {

        LocalDateTime now =
                LocalDateTime.now();

        return enabled
                && startDate != null
                && endDate != null
                && !now.isBefore(startDate)
                && !now.isAfter(endDate);
    }

    public boolean hasUsageLimit() {

        return usageLimit != null
                && usageLimit > 0;
    }

    public boolean hasReachedUsageLimit() {

        return hasUsageLimit()
                && usedCount >= usageLimit;
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        if (usedCount == null) {
            usedCount = 0;
        }

        if (minimumOrderAmount == null) {
            minimumOrderAmount =
                    BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
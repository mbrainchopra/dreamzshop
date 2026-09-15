package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.dreamzshop.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "offers",
        indexes = {
                @Index(
                        name = "idx_offer_enabled",
                        columnList = "enabled"
                ),
                @Index(
                        name = "idx_offer_dates",
                        columnList = "start_date,end_date"
                ),
                @Index(
                        name = "idx_offer_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_offer_category",
                        columnList = "category_id"
                ),
                @Index(
                        name = "idx_offer_brand",
                        columnList = "brand_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(
            message = "Offer name is required"
    )
    @Size(
            min = 2,
            max = 150,
            message = "Offer name must be between 2 and 150 characters"
    )
    @Column(
            nullable = false,
            length = 150
    )
    private String name;


    @Column(
            columnDefinition = "TEXT"
    )
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


    @NotNull(
            message = "Discount value is required"
    )
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


    /*
     * Product-specific offer.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            foreignKey = @ForeignKey(
                    name = "fk_offer_product"
            )
    )
    private Product product;


    /*
     * Category-specific offer.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "category_id",
            foreignKey = @ForeignKey(
                    name = "fk_offer_category"
            )
    )
    private Category category;


    /*
     * Brand-specific offer.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "brand_id",
            foreignKey = @ForeignKey(
                    name = "fk_offer_brand"
            )
    )
    private Brand brand;


    @NotNull(
            message = "Start date is required"
    )
    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDateTime startDate;


    @NotNull(
            message = "End date is required"
    )
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


    /*
     * Checks whether the offer is currently active.
     */
    public boolean isCurrentlyActive() {

        LocalDateTime now =
                LocalDateTime.now();

        return enabled
                && startDate != null
                && endDate != null
                && !now.isBefore(startDate)
                && !now.isAfter(endDate);
    }


    /*
     * Determines whether this offer is global.
     */
    public boolean isGlobalOffer() {

        return product == null
                && category == null
                && brand == null;
    }


    /*
     * Determines whether this offer has a target.
     */
    public boolean hasTarget() {

        return product != null
                || category != null
                || brand != null;
    }


    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        if (minimumOrderAmount == null) {
            minimumOrderAmount =
                    BigDecimal.ZERO;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }

}
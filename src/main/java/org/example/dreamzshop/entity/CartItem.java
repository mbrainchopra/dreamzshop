package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cart_product",
                        columnNames = {
                                "cart_id",
                                "product_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_cart_item_cart",
                        columnList = "cart_id"
                ),
                @Index(
                        name = "idx_cart_item_product",
                        columnList = "product_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // CART
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cart_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_cart_item_cart"
            )
    )
    private Cart cart;

    // =========================================================
    // PRODUCT
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_cart_item_product"
            )
    )
    private Product product;

    // =========================================================
    // QUANTITY
    // =========================================================

    @Min(
            value = 1,
            message = "Quantity must be at least 1"
    )
    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    // =========================================================
    // PRICE SNAPSHOT
    // =========================================================

    @Column(
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal unitPrice;

    // =========================================================
    // AUDIT
    // =========================================================

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // =========================================================
    // BUSINESS HELPERS
    // =========================================================

    public BigDecimal getSubtotal() {

        if (unitPrice == null
                || quantity == null) {

            return BigDecimal.ZERO;
        }

        return unitPrice.multiply(
                BigDecimal.valueOf(quantity)
        );
    }

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
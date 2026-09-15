package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import org.example.dreamzshop.enums.ProductStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_sku", columnList = "sku"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_subcategory", columnList = "subcategory_id"),
                @Index(name = "idx_product_brand", columnList = "brand_id"),
                @Index(name = "idx_product_status", columnList = "status"),
                @Index(name = "idx_product_featured", columnList = "featured")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // BASIC INFORMATION
    // ===============================

    @NotBlank(message = "Product name is required")
    @Size(
            min = 2,
            max = 200,
            message = "Product name must be between 2 and 200 characters"
    )
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(
            min = 2,
            max = 100,
            message = "SKU must be between 2 and 100 characters"
    )
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Size(
            max = 100,
            message = "Barcode cannot exceed 100 characters"
    )
    @Column(unique = true, length = 100)
    private String barcode;

    @Size(
            max = 5000,
            message = "Description cannot exceed 5000 characters"
    )
    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(
            max = 2000,
            message = "Short description cannot exceed 2000 characters"
    )
    @Column(length = 2000)
    private String shortDescription;

    // ===============================
    // CATEGORY
    // ===============================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_product_category"
            )
    )
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "subcategory_id",
            foreignKey = @ForeignKey(
                    name = "fk_product_subcategory"
            )
    )
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "brand_id",
            foreignKey = @ForeignKey(
                    name = "fk_product_brand"
            )
    )
    private Brand brand;

    // ===============================
    // PRICING
    // ===============================

    @NotNull(message = "MRP is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "MRP cannot be negative"
    )
    @Digits(
            integer = 12,
            fraction = 2,
            message = "Invalid MRP"
    )
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal mrp;

    @NotNull(message = "Selling price is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Selling price cannot be negative"
    )
    @Digits(
            integer = 12,
            fraction = 2,
            message = "Invalid selling price"
    )
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal sellingPrice;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Cost price cannot be negative"
    )
    @Digits(
            integer = 12,
            fraction = 2,
            message = "Invalid cost price"
    )
    @Column(precision = 14, scale = 2)
    private BigDecimal costPrice;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Tax percentage cannot be negative"
    )
    @Digits(
            integer = 5,
            fraction = 2,
            message = "Invalid tax percentage"
    )
    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    // ===============================
    // INVENTORY
    // ===============================

    @NotNull(message = "Stock quantity is required")
    @Min(
            value = 0,
            message = "Stock cannot be negative"
    )
    @Column(nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @NotNull(message = "Minimum stock level is required")
    @Min(
            value = 0,
            message = "Minimum stock level cannot be negative"
    )
    @Column(nullable = false)
    @Builder.Default
    private Integer minimumStockLevel = 5;

    // ===============================
    // UNIT / WEIGHT
    // ===============================

    @Size(
            max = 50,
            message = "Unit cannot exceed 50 characters"
    )
    @Column(length = 50)
    private String unit;

    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Weight must be greater than zero"
    )
    @Digits(
            integer = 10,
            fraction = 3,
            message = "Invalid weight"
    )
    @Column(precision = 13, scale = 3)
    private BigDecimal weight;

    @Size(
            max = 20,
            message = "Weight unit cannot exceed 20 characters"
    )
    @Column(length = 20)
    private String weightUnit;

    // ===============================
    // PRODUCT STATUS
    // ===============================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean taxable = true;

    // ===============================
    // MAIN IMAGE
    // ===============================

    @Column(length = 500)
    private String mainImage;

    // ===============================
    // SEO
    // ===============================

    @Size(
            max = 200,
            message = "Meta title cannot exceed 200 characters"
    )
    @Column(length = 200)
    private String metaTitle;

    @Size(
            max = 500,
            message = "Meta description cannot exceed 500 characters"
    )
    @Column(length = 500)
    private String metaDescription;

    @Size(
            max = 200,
            message = "Slug cannot exceed 200 characters"
    )
    @Column(unique = true, length = 200)
    private String slug;

    // ===============================
    // PRODUCT IMAGES
    // ===============================

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    // ===============================
    // AUDIT
    // ===============================

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // ===============================
    // BUSINESS HELPERS
    // ===============================

    public boolean isInStock() {

        return stockQuantity != null
                && stockQuantity > 0;
    }

    public boolean isLowStock() {

        return stockQuantity != null
                && minimumStockLevel != null
                && stockQuantity > 0
                && stockQuantity <= minimumStockLevel;
    }

    public BigDecimal getDiscountAmount() {

        if (mrp == null || sellingPrice == null) {
            return BigDecimal.ZERO;
        }

        return mrp.subtract(sellingPrice)
                .max(BigDecimal.ZERO);
    }

    public BigDecimal getDiscountPercentage() {

        if (mrp == null
                || sellingPrice == null
                || mrp.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        return mrp.subtract(sellingPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        mrp,
                        2,
                        RoundingMode.HALF_UP
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

    // ===============================
    // IMAGE HELPER METHODS
    // ===============================

    public void addImage(ProductImage image) {

        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {

        images.remove(image);
        image.setProduct(null);
    }
}
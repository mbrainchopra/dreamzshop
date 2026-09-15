package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(
                        name = "idx_product_image_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_product_image_primary",
                        columnList = "primary_image"
                ),
                @Index(
                        name = "idx_product_image_sort",
                        columnList = "sort_order"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // PRODUCT
    // ===============================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_product_image_product"
            )
    )
    private Product product;

    // ===============================
    // IMAGE
    // ===============================

    @NotBlank(message = "Image URL is required")
    @Size(
            max = 500,
            message = "Image URL cannot exceed 500 characters"
    )
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Size(
            max = 200,
            message = "Alt text cannot exceed 200 characters"
    )
    @Column(length = 200)
    private String altText;

    // ===============================
    // IMAGE SETTINGS
    // ===============================

    @Column(nullable = false)
    @Builder.Default
    private boolean primaryImage = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // ===============================
    // AUDIT
    // ===============================

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // ===============================
    // LIFECYCLE
    // ===============================

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
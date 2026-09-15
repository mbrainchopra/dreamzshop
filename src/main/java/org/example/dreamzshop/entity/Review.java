package org.example.dreamzshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.dreamzshop.enums.ReviewStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(
                        name = "idx_review_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_review_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_review_status",
                        columnList = "status"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_product_order",
                        columnNames = {
                                "user_id",
                                "product_id",
                                "order_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * Customer who submitted the review.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    /*
     * Product being reviewed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;


    /*
     * Order from which the product was purchased.
     *
     * This allows us to verify that the customer
     * actually purchased the product.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;


    /*
     * Rating from 1 to 5.
     */
    @NotNull
    @Min(1)
    @Max(5)
    @Column(
            name = "rating",
            nullable = false
    )
    private Integer rating;


    /*
     * Review title.
     */
    @Size(
            max = 150,
            message = "Review title cannot exceed 150 characters"
    )
    @Column(
            name = "title",
            length = 150
    )
    private String title;


    /*
     * Review content.
     */
    @NotNull
    @Size(
            min = 5,
            max = 2000,
            message = "Review must contain between 5 and 2000 characters"
    )
    @Column(
            name = "comment",
            nullable = false,
            length = 2000
    )
    private String comment;


    /*
     * Admin moderation status.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private ReviewStatus status =
            ReviewStatus.PENDING;


    /*
     * Admin moderation remarks.
     */
    @Size(
            max = 1000,
            message = "Admin remarks cannot exceed 1000 characters"
    )
    @Column(
            name = "admin_remarks",
            length = 1000
    )
    private String adminRemarks;


    /*
     * Indicates that this review belongs to
     * a verified purchase.
     */
    @Column(
            name = "verified_purchase",
            nullable = false
    )
    @Builder.Default
    private boolean verifiedPurchase = true;


    /*
     * Whether the review is currently visible
     * to customers.
     */
    @Column(
            name = "visible",
            nullable = false
    )
    @Builder.Default
    private boolean visible = false;


    /*
     * Created date.
     */
    @Column(
            name = "created_at",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();


    /*
     * Last updated date.
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
            status = ReviewStatus.PENDING;
        }

        /*
         * New reviews should not become public
         * before admin approval.
         */
        if (status != ReviewStatus.APPROVED) {
            visible = false;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();

        /*
         * Only approved reviews can be visible.
         */
        if (status != ReviewStatus.APPROVED) {
            visible = false;
        }
    }


    /*
     * Mark review as approved.
     */
    public void approve() {

        this.status =
                ReviewStatus.APPROVED;

        this.visible = true;

        this.updatedAt =
                LocalDateTime.now();
    }


    /*
     * Mark review as rejected.
     */
    public void reject(
            String remarks
    ) {

        this.status =
                ReviewStatus.REJECTED;

        this.visible = false;

        this.adminRemarks =
                remarks;

        this.updatedAt =
                LocalDateTime.now();
    }


    /*
     * Hide an already approved review.
     */
    public void hide() {

        this.status =
                ReviewStatus.HIDDEN;

        this.visible = false;

        this.updatedAt =
                LocalDateTime.now();
    }
}
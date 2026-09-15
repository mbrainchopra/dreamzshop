package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Review;
import org.example.dreamzshop.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    /*
     * Customer reviews.
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );


    /*
     * All reviews for admin.
     */
    Page<Review> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );


    /*
     * Reviews filtered by status.
     */
    Page<Review> findByStatusOrderByCreatedAtDesc(
            ReviewStatus status,
            Pageable pageable
    );


    /*
     * Reviews for a particular product.
     *
     * Only approved and visible reviews are returned
     * for the customer-facing product page.
     */
    Page<Review> findByProductIdAndStatusAndVisibleTrueOrderByCreatedAtDesc(
            Long productId,
            ReviewStatus status,
            Pageable pageable
    );


    /*
     * Check whether a customer has already reviewed
     * a particular product from a particular order.
     */
    boolean existsByUserIdAndProductIdAndOrderId(
            Long userId,
            Long productId,
            Long orderId
    );


    /*
     * Find a specific customer's review for an order/product.
     */
    Optional<Review> findByUserIdAndProductIdAndOrderId(
            Long userId,
            Long productId,
            Long orderId
    );


    /*
     * Customer ownership check.
     */
    Optional<Review> findByIdAndUserId(
            Long id,
            Long userId
    );


    /*
     * Product review count.
     */
    long countByProductIdAndStatusAndVisibleTrue(
            Long productId,
            ReviewStatus status
    );


    /*
     * Admin dashboard counts.
     */
    long countByStatus(
            ReviewStatus status
    );


    /*
     * Average rating of approved visible reviews.
     */
    @Query("""
            SELECT COALESCE(AVG(r.rating), 0)
            FROM Review r
            WHERE r.product.id = :productId
              AND r.status = :status
              AND r.visible = true
            """)
    Double findAverageRatingByProductIdAndStatus(
            Long productId,
            ReviewStatus status
    );


    /*
     * Total approved visible reviews for a product.
     */
    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.product.id = :productId
              AND r.status = :status
              AND r.visible = true
            """)
    long countApprovedVisibleReviews(
            Long productId,
            ReviewStatus status
    );


    /*
     * Rating distribution.
     */
    @Query("""
            SELECT COUNT(r)
            FROM Review r
            WHERE r.product.id = :productId
              AND r.rating = :rating
              AND r.status = :status
              AND r.visible = true
            """)
    long countByProductIdAndRatingAndStatus(
            Long productId,
            Integer rating,
            ReviewStatus status
    );
}
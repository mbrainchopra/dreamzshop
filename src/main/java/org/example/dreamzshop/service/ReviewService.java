package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Review;
import org.example.dreamzshop.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /*
     * Customer creates a review for a purchased product.
     */
    Review createReview(
            String email,
            Long orderId,
            Long productId,
            Integer rating,
            String title,
            String comment
    );


    /*
     * Get customer's own reviews.
     */
    Page<Review> getCustomerReviews(
            String email,
            Pageable pageable
    );


    /*
     * Get one review belonging to the customer.
     */
    Review getCustomerReview(
            String email,
            Long reviewId
    );


    /*
     * Get approved and visible reviews
     * for a product.
     */
    Page<Review> getProductReviews(
            Long productId,
            Pageable pageable
    );


    /*
     * Get all reviews for admin.
     */
    Page<Review> getAllReviews(
            Pageable pageable
    );


    /*
     * Get admin reviews filtered by status.
     */
    Page<Review> getReviewsByStatus(
            ReviewStatus status,
            Pageable pageable
    );


    /*
     * Get a review by ID for admin.
     */
    Review getReview(
            Long reviewId
    );


    /*
     * Update review moderation status.
     */
    void updateReviewStatus(
            Long reviewId,
            ReviewStatus newStatus,
            String adminRemarks
    );


    /*
     * Delete customer's own review.
     */
    void deleteCustomerReview(
            String email,
            Long reviewId
    );


    /*
     * Check whether customer can review
     * a product from an order.
     */
    boolean canReviewProduct(
            String email,
            Long orderId,
            Long productId
    );


    /*
     * Product rating summary.
     */
    Double getAverageRating(
            Long productId
    );


    /*
     * Total approved visible reviews.
     */
    long getReviewCount(
            Long productId
    );


    /*
     * Rating distribution.
     */
    long getRatingCount(
            Long productId,
            Integer rating
    );


    /*
     * Admin dashboard statistics.
     */
    long getPendingReviewCount();

    long getApprovedReviewCount();

    long getRejectedReviewCount();

    long getHiddenReviewCount();
}
package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.OrderItem;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.Review;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.NotificationType;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.ReviewStatus;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.OrderItemRepository;
import org.example.dreamzshop.repository.OrderRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.repository.ReviewRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.NotificationService;
import org.example.dreamzshop.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;


    // =========================================================
    // CUSTOMER - CREATE REVIEW
    // =========================================================

    @Override
    public Review createReview(
            String email,
            Long orderId,
            Long productId,
            Integer rating,
            String title,
            String comment
    ) {

        User user = getCustomer(email);

        if (orderId == null) {
            throw new IllegalArgumentException(
                    "Order ID is required."
            );
        }

        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID is required."
            );
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5."
            );
        }

        if (comment == null || comment.trim().length() < 5) {
            throw new IllegalArgumentException(
                    "Review comment must contain at least 5 characters."
            );
        }

        Order order = orderRepository
                .findByIdAndUserId(
                        orderId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found."
                        )
                );

        /*
         * Only delivered orders can be reviewed.
         */
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "You can review products only after the order is delivered."
            );
        }

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found."
                        )
                );

        /*
         * Verify that the product actually belongs
         * to this order.
         */
        boolean purchased = orderItemRepository
                .findByOrderId(orderId)
                .stream()
                .anyMatch(item ->
                        item.getProduct() != null
                                && item.getProduct()
                                .getId()
                                .equals(productId)
                );

        if (!purchased) {
            throw new IllegalArgumentException(
                    "You can review only products purchased in this order."
            );
        }

        /*
         * Prevent duplicate review for the same
         * customer + product + order.
         */
        if (reviewRepository
                .existsByUserIdAndProductIdAndOrderId(
                        user.getId(),
                        productId,
                        orderId
                )) {

            throw new IllegalArgumentException(
                    "You have already reviewed this product for this order."
            );
        }

        String cleanedTitle =
                title == null
                        ? null
                        : title.trim();

        String cleanedComment =
                comment.trim();

        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(rating)
                .title(
                        cleanedTitle == null
                                || cleanedTitle.isBlank()
                                ? null
                                : cleanedTitle
                )
                .comment(cleanedComment)
                .status(ReviewStatus.PENDING)
                .adminRemarks(null)
                .verifiedPurchase(true)
                .visible(false)
                .build();

        return reviewRepository.save(review);
    }


    // =========================================================
    // CUSTOMER - LIST REVIEWS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getCustomerReviews(
            String email,
            Pageable pageable
    ) {

        User user = getCustomer(email);

        return reviewRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );
    }


    // =========================================================
    // CUSTOMER - SINGLE REVIEW
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Review getCustomerReview(
            String email,
            Long reviewId
    ) {

        User user = getCustomer(email);

        return reviewRepository
                .findByIdAndUserId(
                        reviewId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Review not found."
                        )
                );
    }


    // =========================================================
    // CUSTOMER - PRODUCT REVIEWS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getProductReviews(
            Long productId,
            Pageable pageable
    ) {

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Product not found."
            );
        }

        return reviewRepository
                .findByProductIdAndStatusAndVisibleTrueOrderByCreatedAtDesc(
                        productId,
                        ReviewStatus.APPROVED,
                        pageable
                );
    }


    // =========================================================
    // ADMIN - ALL REVIEWS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(
            Pageable pageable
    ) {

        return reviewRepository
                .findAllByOrderByCreatedAtDesc(
                        pageable
                );
    }


    // =========================================================
    // ADMIN - FILTER BY STATUS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByStatus(
            ReviewStatus status,
            Pageable pageable
    ) {

        if (status == null) {
            return getAllReviews(pageable);
        }

        return reviewRepository
                .findByStatusOrderByCreatedAtDesc(
                        status,
                        pageable
                );
    }


    // =========================================================
    // ADMIN - SINGLE REVIEW
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Review getReview(
            Long reviewId
    ) {

        return reviewRepository
                .findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Review not found."
                        )
                );
    }


    // =========================================================
    // ADMIN - MODERATE REVIEW
    // =========================================================

    @Override
    public void updateReviewStatus(
            Long reviewId,
            ReviewStatus newStatus,
            String adminRemarks
    ) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Review status is required."
            );
        }

        Review review = getReview(reviewId);

        String remarks =
                adminRemarks == null
                        ? null
                        : adminRemarks.trim();

        ReviewStatus oldStatus = review.getStatus();

        switch (newStatus) {

            case APPROVED -> {

                review.setStatus(
                        ReviewStatus.APPROVED
                );

                review.setVisible(true);

                review.setAdminRemarks(
                        remarks
                );

                /*
                 * Notify customer only when the review
                 * actually becomes approved.
                 */
                if (oldStatus != ReviewStatus.APPROVED) {

                    createReviewNotificationIfNotExists(
                            review,
                            NotificationType.REVIEW_APPROVED,
                            "Review Approved",
                            "Your review for \""
                                    + getProductName(review)
                                    + "\" has been approved and is now visible."
                    );
                }
            }

            case REJECTED -> {

                review.setStatus(
                        ReviewStatus.REJECTED
                );

                review.setVisible(false);

                review.setAdminRemarks(
                        remarks
                );

                /*
                 * Notify customer only when the review
                 * actually becomes rejected.
                 */
                if (oldStatus != ReviewStatus.REJECTED) {

                    createReviewNotificationIfNotExists(
                            review,
                            NotificationType.REVIEW_REJECTED,
                            "Review Rejected",
                            "Your review for \""
                                    + getProductName(review)
                                    + "\" was rejected by the admin."
                    );
                }
            }

            case HIDDEN -> {

                review.setStatus(
                        ReviewStatus.HIDDEN
                );

                review.setVisible(false);

                review.setAdminRemarks(
                        remarks
                );
            }

            case PENDING -> {

                review.setStatus(
                        ReviewStatus.PENDING
                );

                review.setVisible(false);

                review.setAdminRemarks(
                        remarks
                );
            }

            default -> throw new IllegalArgumentException(
                    "Invalid review status."
            );
        }

        reviewRepository.save(review);
    }


    // =========================================================
    // CUSTOMER - DELETE REVIEW
    // =========================================================

    @Override
    public void deleteCustomerReview(
            String email,
            Long reviewId
    ) {

        Review review =
                getCustomerReview(
                        email,
                        reviewId
                );

        reviewRepository.delete(review);
    }


    // =========================================================
    // CUSTOMER - CAN REVIEW?
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public boolean canReviewProduct(
            String email,
            Long orderId,
            Long productId
    ) {

        User user = getCustomer(email);

        if (orderId == null || productId == null) {
            return false;
        }

        OptionalOrderData data =
                findOrderAndProduct(
                        user.getId(),
                        orderId,
                        productId
                );

        if (!data.valid()) {
            return false;
        }

        return !reviewRepository
                .existsByUserIdAndProductIdAndOrderId(
                        user.getId(),
                        productId,
                        orderId
                );
    }


    // =========================================================
    // PRODUCT - AVERAGE RATING
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(
            Long productId
    ) {

        Double average =
                reviewRepository
                        .findAverageRatingByProductIdAndStatus(
                                productId,
                                ReviewStatus.APPROVED
                        );

        if (average == null) {
            return 0.0;
        }

        return Math.round(
                average * 10.0
        ) / 10.0;
    }


    // =========================================================
    // PRODUCT - REVIEW COUNT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public long getReviewCount(
            Long productId
    ) {

        return reviewRepository
                .countByProductIdAndStatusAndVisibleTrue(
                        productId,
                        ReviewStatus.APPROVED
                );
    }


    // =========================================================
    // PRODUCT - RATING COUNT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public long getRatingCount(
            Long productId,
            Integer rating
    ) {

        if (rating == null || rating < 1 || rating > 5) {
            return 0;
        }

        return reviewRepository
                .countByProductIdAndRatingAndStatus(
                        productId,
                        rating,
                        ReviewStatus.APPROVED
                );
    }


    // =========================================================
    // ADMIN DASHBOARD COUNTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public long getPendingReviewCount() {

        return reviewRepository
                .countByStatus(
                        ReviewStatus.PENDING
                );
    }


    @Override
    @Transactional(readOnly = true)
    public long getApprovedReviewCount() {

        return reviewRepository
                .countByStatus(
                        ReviewStatus.APPROVED
                );
    }


    @Override
    @Transactional(readOnly = true)
    public long getRejectedReviewCount() {

        return reviewRepository
                .countByStatus(
                        ReviewStatus.REJECTED
                );
    }


    @Override
    @Transactional(readOnly = true)
    public long getHiddenReviewCount() {

        return reviewRepository
                .countByStatus(
                        ReviewStatus.HIDDEN
                );
    }


    // =========================================================
    // PRIVATE - CREATE REVIEW NOTIFICATION
    // =========================================================

    private void createReviewNotificationIfNotExists(
            Review review,
            NotificationType type,
            String title,
            String message
    ) {

        if (review.getUser() == null
                || review.getUser().getId() == null
                || review.getId() == null) {
            return;
        }

        String referenceKey =
                "REVIEW:"
                        + review.getId()
                        + ":"
                        + type.name();

        String actionUrl =
                "/customer/reviews/"
                        + review.getId();

        notificationService.createNotificationIfNotExists(
                review.getUser().getId(),
                type,
                title,
                message,
                actionUrl,
                review.getId(),
                referenceKey
        );
    }


    // =========================================================
    // PRIVATE - PRODUCT NAME
    // =========================================================

    private String getProductName(
            Review review
    ) {

        if (review.getProduct() != null
                && review.getProduct().getName() != null) {

            return review.getProduct().getName();
        }

        return "your purchased product";
    }


    // =========================================================
    // PRIVATE - CUSTOMER VALIDATION
    // =========================================================

    private User getCustomer(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "User email is required."
            );
        }

        User user = userRepository
                .findByEmail(
                        email.trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found."
                        )
                );

        if (user.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Only customers can perform this action."
            );
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "Your account is disabled."
            );
        }

        return user;
    }


    // =========================================================
    // PRIVATE - ORDER + PRODUCT VALIDATION
    // =========================================================

    private OptionalOrderData findOrderAndProduct(
            Long userId,
            Long orderId,
            Long productId
    ) {

        Optional<Order> orderOptional =
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                );

        if (orderOptional.isEmpty()) {
            return new OptionalOrderData(
                    false,
                    null,
                    null
            );
        }

        Order order =
                orderOptional.get();

        if (order.getOrderStatus()
                != OrderStatus.DELIVERED) {

            return new OptionalOrderData(
                    false,
                    order,
                    null
            );
        }

        Optional<Product> productOptional =
                productRepository.findById(
                        productId
                );

        if (productOptional.isEmpty()) {
            return new OptionalOrderData(
                    false,
                    order,
                    null
            );
        }

        Product product =
                productOptional.get();

        boolean purchased =
                orderItemRepository
                        .findByOrderId(orderId)
                        .stream()
                        .anyMatch(item ->
                                item.getProduct() != null
                                        && item.getProduct()
                                        .getId()
                                        .equals(product.getId())
                        );

        if (!purchased) {
            return new OptionalOrderData(
                    false,
                    order,
                    product
            );
        }

        return new OptionalOrderData(
                true,
                order,
                product
        );
    }


    // =========================================================
    // INTERNAL RECORD
    // =========================================================

    private record OptionalOrderData(
            boolean valid,
            Order order,
            Product product
    ) {
    }
}
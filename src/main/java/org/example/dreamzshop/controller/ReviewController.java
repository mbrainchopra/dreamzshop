package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Review;
import org.example.dreamzshop.enums.ReviewStatus;
import org.example.dreamzshop.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    // =========================================================
    // CUSTOMER REVIEWS
    // =========================================================

    /**
     * Display customer's submitted reviews.
     */
    @GetMapping("/customer/reviews")
    public String customerReviews(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        if (page < 0) {
            page = 0;
        }

        PageRequest pageable =
                PageRequest.of(
                        page,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        try {

            String email =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getName();

            Page<Review> reviews =
                    reviewService.getCustomerReviews(
                            email,
                            pageable
                    );

            model.addAttribute(
                    "reviews",
                    reviews
            );

            return "customer/reviews";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "customer/reviews";
        }
    }


    // =========================================================
    // CUSTOMER - REVIEW FORM
    // =========================================================

    /**
     * Open review form for a purchased product.
     */
    @GetMapping("/customer/reviews/write")
    public String writeReview(
            @RequestParam Long orderId,
            @RequestParam Long productId,
            Model model
    ) {

        try {

            String email =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext()
                            .getAuthentication()
                            .getName();

            if (!reviewService.canReviewProduct(
                    email,
                    orderId,
                    productId
            )) {

                model.addAttribute(
                        "error",
                        "You cannot review this product."
                );

                return "redirect:/customer/orders/" + orderId;
            }

            Review review =
                    Review.builder()
                            .rating(5)
                            .build();

            model.addAttribute(
                    "review",
                    review
            );

            model.addAttribute(
                    "orderId",
                    orderId
            );

            model.addAttribute(
                    "productId",
                    productId
            );

            return "customer/review-form";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/orders/" + orderId;
        }
    }


    // =========================================================
    // CUSTOMER - SAVE REVIEW
    // =========================================================

    /**
     * Submit a new product review.
     */
    @PostMapping("/customer/reviews/save")
    public String saveReview(
            @RequestParam Long orderId,
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String title,
            @RequestParam String comment,
            Model model
    ) {

        String email =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        try {

            Review review =
                    reviewService.createReview(
                            email,
                            orderId,
                            productId,
                            rating,
                            title,
                            comment
                    );

            return "redirect:/customer/reviews?success=Review+submitted+successfully";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "review",
                    Review.builder()
                            .rating(rating)
                            .title(title)
                            .comment(comment)
                            .build()
            );

            model.addAttribute(
                    "orderId",
                    orderId
            );

            model.addAttribute(
                    "productId",
                    productId
            );

            return "customer/review-form";
        }
    }


    // =========================================================
    // CUSTOMER - REVIEW DETAILS
    // =========================================================

    /**
     * Display one customer's review.
     */
    @GetMapping("/customer/reviews/{id}")
    public String reviewDetails(
            @PathVariable Long id,
            Model model
    ) {

        String email =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        try {

            Review review =
                    reviewService.getCustomerReview(
                            email,
                            id
                    );

            model.addAttribute(
                    "review",
                    review
            );

            return "customer/review-details";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/reviews?error=Review+not+found";
        }
    }


    // =========================================================
    // CUSTOMER - DELETE REVIEW
    // =========================================================

    /**
     * Delete customer's own review.
     */
    @PostMapping("/customer/reviews/{id}/delete")
    public String deleteReview(
            @PathVariable Long id
    ) {

        String email =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        try {

            reviewService.deleteCustomerReview(
                    email,
                    id
            );

            return "redirect:/customer/reviews?success=Review+deleted+successfully";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/reviews?error="
                    + e.getMessage();
        }
    }


    // =========================================================
    // ADMIN - REVIEW MANAGEMENT
    // =========================================================

    /**
     * Display all reviews for admin.
     */
    @GetMapping("/admin/reviews")
    public String adminReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        if (page < 0) {
            page = 0;
        }

        PageRequest pageable =
                PageRequest.of(
                        page,
                        15,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<Review> reviews;

        if (status == null) {

            reviews =
                    reviewService.getAllReviews(
                            pageable
                    );

        } else {

            reviews =
                    reviewService.getReviewsByStatus(
                            status,
                            pageable
                    );
        }

        model.addAttribute(
                "reviews",
                reviews
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "statuses",
                ReviewStatus.values()
        );

        model.addAttribute(
                "pendingCount",
                reviewService.getPendingReviewCount()
        );

        model.addAttribute(
                "approvedCount",
                reviewService.getApprovedReviewCount()
        );

        model.addAttribute(
                "rejectedCount",
                reviewService.getRejectedReviewCount()
        );

        model.addAttribute(
                "hiddenCount",
                reviewService.getHiddenReviewCount()
        );

        return "admin/reviews";
    }


    // =========================================================
    // ADMIN - REVIEW DETAILS
    // =========================================================

    /**
     * Display review details.
     */
    @GetMapping("/admin/reviews/{id}")
    public String adminReviewDetails(
            @PathVariable Long id,
            Model model
    ) {

        try {

            Review review =
                    reviewService.getReview(id);

            model.addAttribute(
                    "review",
                    review
            );

            model.addAttribute(
                    "statuses",
                    ReviewStatus.values()
            );

            return "admin/review-details";

        } catch (IllegalArgumentException e) {

            return "redirect:/admin/reviews?error=Review+not+found";
        }
    }


    // =========================================================
    // ADMIN - UPDATE REVIEW STATUS
    // =========================================================

    /**
     * Approve, reject, hide or move review back to pending.
     */
    @PostMapping("/admin/reviews/{id}/status")
    public String updateReviewStatus(
            @PathVariable Long id,
            @RequestParam ReviewStatus status,
            @RequestParam(required = false) String adminRemarks
    ) {

        try {

            reviewService.updateReviewStatus(
                    id,
                    status,
                    adminRemarks
            );

            return "redirect:/admin/reviews/"
                    + id
                    + "?success=Review+status+updated";

        } catch (IllegalArgumentException e) {

            return "redirect:/admin/reviews/"
                    + id
                    + "?error="
                    + e.getMessage();
        }
    }
}
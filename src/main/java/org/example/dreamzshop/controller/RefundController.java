package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Refund;
import org.example.dreamzshop.enums.RefundStatus;
import org.example.dreamzshop.service.RefundService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;


    /* =========================================================
       CUSTOMER REFUNDS
       ========================================================= */

    @GetMapping("/customer/refunds")
    public String customerRefunds(
            Authentication authentication,
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

        Page<Refund> refunds =
                refundService.getCustomerRefunds(
                        authentication.getName(),
                        pageable
                );

        model.addAttribute(
                "refunds",
                refunds
        );

        return "customer/refunds";
    }


    @GetMapping("/customer/refunds/{id}")
    public String customerRefundDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {

        try {

            Refund refund =
                    refundService.getCustomerRefund(
                            authentication.getName(),
                            id
                    );

            model.addAttribute(
                    "refund",
                    refund
            );

            return "customer/refund-details";

        } catch (IllegalArgumentException ex) {

            return "redirect:/customer/refunds?error="
                    + encodeMessage(ex.getMessage());
        }
    }


    /* =========================================================
       ADMIN REFUNDS
       ========================================================= */

    @GetMapping("/admin/refunds")
    public String adminRefunds(
            @RequestParam(required = false) RefundStatus status,
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

        Page<Refund> refunds;

        if (status == null) {

            refunds =
                    refundService.getAllRefunds(
                            pageable
                    );

        } else {

            refunds =
                    refundService.getRefundsByStatus(
                            status,
                            pageable
                    );
        }


        /* =====================================================
           STATUS COUNTS
           ===================================================== */

        long pendingCount =
                refundService
                        .getRefundsByStatus(
                                RefundStatus.PENDING,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements();


        long initiatedCount =
                refundService
                        .getRefundsByStatus(
                                RefundStatus.INITIATED,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements();


        long processingCount =
                refundService
                        .getRefundsByStatus(
                                RefundStatus.PROCESSING,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements();


        long completedCount =
                refundService
                        .getRefundsByStatus(
                                RefundStatus.COMPLETED,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements();


        long failedCount =
                refundService
                        .getRefundsByStatus(
                                RefundStatus.FAILED,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements();


        model.addAttribute(
                "refunds",
                refunds
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "statuses",
                RefundStatus.values()
        );

        model.addAttribute(
                "pendingCount",
                pendingCount
        );

        model.addAttribute(
                "initiatedCount",
                initiatedCount
        );

        model.addAttribute(
                "processingCount",
                processingCount
        );

        model.addAttribute(
                "completedCount",
                completedCount
        );

        model.addAttribute(
                "failedCount",
                failedCount
        );


        return "admin/refunds";
    }


    /* =========================================================
       ADMIN REFUND DETAILS
       ========================================================= */

    @GetMapping("/admin/refunds/{id}")
    public String adminRefundDetails(
            @PathVariable Long id,
            Model model
    ) {

        try {

            Refund refund =
                    refundService.getRefund(id);

            model.addAttribute(
                    "refund",
                    refund
            );

            model.addAttribute(
                    "statuses",
                    RefundStatus.values()
            );

            return "admin/refund-details";

        } catch (IllegalArgumentException ex) {

            return "redirect:/admin/refunds?error="
                    + encodeMessage(ex.getMessage());
        }
    }


    /* =========================================================
       FIX:
       HANDLE DIRECT GET REQUEST TO /status
       ========================================================= */

    @GetMapping("/admin/refunds/{id}/status")
    public String refundStatusGet(
            @PathVariable Long id
    ) {

        /*
         * This URL is supposed to be used by the POST form.
         *
         * If somebody:
         * - refreshes the URL
         * - opens it directly
         * - pastes it into the browser
         *
         * redirect them safely back to refund details.
         */

        return "redirect:/admin/refunds/" + id;
    }


    /* =========================================================
       ADMIN UPDATE REFUND STATUS
       ========================================================= */

    @PostMapping("/admin/refunds/{id}/status")
    public String updateRefundStatus(
            @PathVariable Long id,

            @RequestParam RefundStatus status,

            @RequestParam(required = false)
            String transactionReference,

            @RequestParam(required = false)
            String remarks
    ) {

        try {

            refundService.updateRefundStatus(
                    id,
                    status,
                    transactionReference,
                    remarks
            );


            return "redirect:/admin/refunds/"
                    + id
                    + "?success="
                    + encodeMessage(
                    "Refund status updated successfully."
            );


        } catch (IllegalArgumentException ex) {

            return "redirect:/admin/refunds/"
                    + id
                    + "?error="
                    + encodeMessage(
                    ex.getMessage()
            );

        } catch (Exception ex) {

            return "redirect:/admin/refunds/"
                    + id
                    + "?error="
                    + encodeMessage(
                    "Unable to update refund status."
            );
        }
    }


    /* =========================================================
       HELPER
       ========================================================= */

    private String encodeMessage(
            String message
    ) {

        if (message == null ||
                message.trim().isEmpty()) {

            return "Something went wrong.";
        }

        return UriUtils.encode(
                message,
                StandardCharsets.UTF_8
        );
    }
}
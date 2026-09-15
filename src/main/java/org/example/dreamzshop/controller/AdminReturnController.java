package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.ReturnRequest;
import org.example.dreamzshop.enums.ReturnRequestStatus;
import org.example.dreamzshop.service.ReturnRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/returns")
public class AdminReturnController {

    private final ReturnRequestService returnRequestService;

    @GetMapping
    public String returns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) ReturnRequestStatus status,
            Model model
    ) {

        if (page < 0) {
            page = 0;
        }

        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<ReturnRequest> returns;

        if (status != null) {
            returns =
                    returnRequestService
                            .getReturnRequestsByStatus(
                                    status,
                                    pageable
                            );
        } else {
            returns =
                    returnRequestService
                            .getAllReturnRequests(
                                    pageable
                            );
        }

        model.addAttribute(
                "returns",
                returns
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "statuses",
                ReturnRequestStatus.values()
        );

        model.addAttribute(
                "requestedCount",
                returnRequestService
                        .getReturnRequestsByStatus(
                                ReturnRequestStatus.REQUESTED,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements()
        );

        model.addAttribute(
                "approvedCount",
                returnRequestService
                        .getReturnRequestsByStatus(
                                ReturnRequestStatus.APPROVED,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements()
        );

        model.addAttribute(
                "completedCount",
                returnRequestService
                        .getReturnRequestsByStatus(
                                ReturnRequestStatus.COMPLETED,
                                PageRequest.of(0, 1)
                        )
                        .getTotalElements()
        );

        return "admin/returns";
    }

    @GetMapping("/{id}")
    public String returnDetails(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            ReturnRequest returnRequest =
                    returnRequestService
                            .getReturnRequest(id);

            model.addAttribute(
                    "returnRequest",
                    returnRequest
            );

            model.addAttribute(
                    "statuses",
                    ReturnRequestStatus.values()
            );

            return "admin/return-details";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/returns";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam ReturnRequestStatus status,
            @RequestParam(required = false) String adminRemarks,
            RedirectAttributes redirectAttributes
    ) {

        try {

            returnRequestService.updateReturnRequestStatus(
                    id,
                    status,
                    adminRemarks
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Return request status updated successfully."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/returns/" + id;
    }
}
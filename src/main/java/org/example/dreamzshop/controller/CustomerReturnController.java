package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.ReturnRequest;
import org.example.dreamzshop.enums.ReturnRequestStatus;
import org.example.dreamzshop.service.ReturnRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/returns")
public class CustomerReturnController {

    private final ReturnRequestService returnRequestService;

    @GetMapping
    public String returns(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication authentication
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

        Page<ReturnRequest> returns =
                returnRequestService.getCustomerReturnRequests(
                        authentication.getName(),
                        pageable
                );

        model.addAttribute(
                "returns",
                returns
        );

        model.addAttribute(
                "currentPage",
                page
        );

        return "customer/returns";
    }

    @GetMapping("/request/{orderId}")
    public String requestForm(
            @PathVariable Long orderId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            model.addAttribute(
                    "orderId",
                    orderId
            );

            return "customer/return-form";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to open return request."
            );

            return "redirect:/customer/orders";
        }
    }

    @PostMapping("/request")
    public String createReturnRequest(
            @RequestParam Long orderId,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            ReturnRequest returnRequest =
                    returnRequestService.createReturnRequest(
                            authentication.getName(),
                            orderId,
                            reason,
                            description
                    );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Return request submitted successfully."
            );

            return "redirect:/customer/returns/"
                    + returnRequest.getId();

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/customer/returns/request/"
                    + orderId;
        }
    }

    @GetMapping("/{id}")
    public String returnDetails(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            ReturnRequest returnRequest =
                    returnRequestService
                            .getCustomerReturnRequest(
                                    authentication.getName(),
                                    id
                            );

            model.addAttribute(
                    "returnRequest",
                    returnRequest
            );

            return "customer/return-details";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/customer/returns";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelReturnRequest(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            returnRequestService.cancelReturnRequest(
                    authentication.getName(),
                    id
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Return request cancelled successfully."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/customer/returns/" + id;
    }

    private boolean isCancellable(
            ReturnRequest returnRequest
    ) {

        if (returnRequest == null
                || returnRequest.getStatus() == null) {

            return false;
        }

        return returnRequest.getStatus()
                == ReturnRequestStatus.REQUESTED;
    }
}
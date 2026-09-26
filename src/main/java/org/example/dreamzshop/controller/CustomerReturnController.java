package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.entity.OrderItem;
import org.example.dreamzshop.entity.ReturnRequest;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.ReturnRequestStatus;
import org.example.dreamzshop.service.OrderService;
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
    private final OrderService orderService;


    // =========================================================
    // CUSTOMER - MY RETURNS
    // =========================================================

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


    // =========================================================
    // CUSTOMER - RETURN FORM WITHOUT ORDER ID
    // =========================================================

    @GetMapping("/request")
    public String requestFormWithoutOrder() {

        return "redirect:/customer/orders";
    }


    // =========================================================
    // CUSTOMER - RETURN FORM
    // =========================================================
    //
    // URL:
    //
    // /customer/returns/request/{orderId}/{orderItemId}
    //
    // Example:
    //
    // /customer/returns/request/15/101
    //
    // =========================================================

    @GetMapping("/request/{orderId}/{orderItemId}")
    public String requestForm(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            // ---------------------------------------------
            // Get customer's order
            // ---------------------------------------------

            Order order =
                    orderService.getCustomerOrder(
                            authentication.getName(),
                            orderId
                    );


            // ---------------------------------------------
            // Order must exist
            // ---------------------------------------------

            if (order == null) {

                throw new IllegalArgumentException(
                        "Order not found."
                );
            }


            // ---------------------------------------------
            // Find selected order item
            // ---------------------------------------------

            OrderItem orderItem =
                    order.getItems()
                            .stream()
                            .filter(item ->
                                    item.getId() != null
                                            && item.getId()
                                            .equals(orderItemId)
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Product not found in this order."
                                    )
                            );


            // ---------------------------------------------
            // Only DELIVERED orders can be returned
            // ---------------------------------------------

            if (order.getOrderStatus() == null
                    || order.getOrderStatus()
                    != OrderStatus.DELIVERED) {

                throw new IllegalArgumentException(
                        "Only delivered orders can be returned."
                );
            }


            // ---------------------------------------------
            // Send order to Thymeleaf
            // ---------------------------------------------

            model.addAttribute(
                    "order",
                    order
            );


            // ---------------------------------------------
            // Send order ID
            // ---------------------------------------------

            model.addAttribute(
                    "orderId",
                    orderId
            );


            // ---------------------------------------------
            // Send order item ID
            // ---------------------------------------------

            model.addAttribute(
                    "orderItemId",
                    orderItemId
            );


            // ---------------------------------------------
            // Send selected order item
            // ---------------------------------------------

            model.addAttribute(
                    "orderItem",
                    orderItem
            );


            // ---------------------------------------------
            // Empty ReturnRequest object
            // ---------------------------------------------

            model.addAttribute(
                    "returnRequest",
                    new ReturnRequest()
            );


            // ---------------------------------------------
            // Open return form
            // ---------------------------------------------

            return "customer/return-form";


        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/customer/orders";


        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to open return request."
            );

            return "redirect:/customer/orders";
        }
    }


    // =========================================================
    // CUSTOMER - CREATE RETURN REQUEST
    // =========================================================

    @PostMapping("/request")
    public String createReturnRequest(
            @RequestParam Long orderId,
            @RequestParam Long orderItemId,
            @RequestParam String reason,
            @RequestParam(required = false) String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            System.out.println("=================================");
            System.out.println("RETURN REQUEST SUBMIT");
            System.out.println("Order ID      : " + orderId);
            System.out.println("Order Item ID : " + orderItemId);
            System.out.println("Reason        : " + reason);
            System.out.println("Description   : " + description);
            System.out.println(
                    "Customer      : "
                            + authentication.getName()
            );
            System.out.println("=================================");


            // ---------------------------------------------
            // Create return request
            // ---------------------------------------------

            ReturnRequest returnRequest =
                    returnRequestService.createReturnRequest(
                            authentication.getName(),
                            orderId,
                            orderItemId,
                            reason,
                            description
                    );


            System.out.println(
                    "RETURN CREATED ID: "
                            + returnRequest.getId()
            );


            // ---------------------------------------------
            // Success message
            // ---------------------------------------------

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Return request submitted successfully."
            );


            // ---------------------------------------------
            // Open return details
            // ---------------------------------------------

            return "redirect:/customer/returns/"
                    + returnRequest.getId();


        } catch (IllegalArgumentException e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );


            // ---------------------------------------------
            // Return to same product return form
            // ---------------------------------------------

            return "redirect:/customer/returns/request/"
                    + orderId
                    + "/"
                    + orderItemId;


        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to submit return request: "
                            + e.getMessage()
            );


            return "redirect:/customer/returns/request/"
                    + orderId
                    + "/"
                    + orderItemId;
        }
    }


    // =========================================================
    // CUSTOMER - RETURN DETAILS
    // =========================================================

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


    // =========================================================
    // CUSTOMER - CANCEL RETURN
    // =========================================================

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


    // =========================================================
    // RETURN REQUEST - CANCELLABLE CHECK
    // =========================================================

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
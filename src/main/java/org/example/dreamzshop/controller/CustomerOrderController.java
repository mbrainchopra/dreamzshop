package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.service.OrderService;
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
@RequestMapping("/customer/orders")
public class CustomerOrderController {

    private final OrderService orderService;

    @GetMapping
    public String orders(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication authentication
    ) {

        if (page < 0) {
            page = 0;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        10,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<Order> orders =
                orderService.getCustomerOrders(
                        authentication.getName(),
                        pageable
                );

        model.addAttribute(
                "orders",
                orders
        );

        model.addAttribute(
                "currentPage",
                page
        );

        return "customer/orders";
    }

    @GetMapping("/{id}")
    public String orderDetails(
            @PathVariable Long id,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Order order =
                    orderService.getCustomerOrder(
                            authentication.getName(),
                            id
                    );

            model.addAttribute(
                    "order",
                    order
            );

            model.addAttribute(
                    "cancellable",
                    isCancellable(order)
            );

            return "customer/order-details";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/customer/orders";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            orderService.cancelCustomerOrder(
                    authentication.getName(),
                    id
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Order cancelled successfully."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/customer/orders/" + id;
    }

    private boolean isCancellable(Order order) {

        if (order == null
                || order.getOrderStatus() == null) {

            return false;
        }

        OrderStatus status =
                order.getOrderStatus();

        return status == OrderStatus.PENDING
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING
                || status == OrderStatus.PACKED;
    }
}
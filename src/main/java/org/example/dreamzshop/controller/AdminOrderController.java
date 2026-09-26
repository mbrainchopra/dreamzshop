package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * Admin order list.
     */
    @GetMapping
    public String orders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 50) {
            size = 10;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<Order> orders;

        if (status != null) {
            orders = orderService.getOrdersByStatus(
                    status,
                    pageable
            );
        } else {
            orders = orderService.getAllOrders(
                    pageable
            );
        }

        model.addAttribute(
                "orders",
                orders
        );

        model.addAttribute(
                "statuses",
                OrderStatus.values()
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "pageSize",
                size
        );

        return "admin/orders";
    }

    /**
     * Admin order details.
     */
    @GetMapping("/{id}")
    public String orderDetails(
            @PathVariable Long id,
            Model model
    ) {

        Order order =
                orderService.getOrderById(id);

        model.addAttribute(
                "order",
                order
        );

        model.addAttribute(
                "statuses",
                allowedNextStatuses(order.getOrderStatus())
        );

        return "admin/order-details";
    }

    /**
     * Update order status.
     */
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes
    ) {

        try {

            orderService.updateOrderStatus(
                    id,
                    status
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Order status updated successfully."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/orders/" + id;
    }
    private java.util.List<OrderStatus> allowedNextStatuses(OrderStatus current) {
        if (current == null) {
            return java.util.List.of();
        }

        java.util.List<OrderStatus> next = switch (current) {
            case PENDING -> java.util.List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> java.util.List.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED);
            case PROCESSING -> java.util.List.of(OrderStatus.PACKED, OrderStatus.CANCELLED);
            case PACKED -> java.util.List.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED);
            case SHIPPED -> java.util.List.of(OrderStatus.OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> java.util.List.of(OrderStatus.DELIVERED);
            case DELIVERED, RETURN_REQUESTED, RETURNED, REFUND_REQUESTED, REFUNDED, CANCELLED -> java.util.List.of();
        };

        java.util.ArrayList<OrderStatus> statuses = new java.util.ArrayList<>();
        statuses.add(current);
        statuses.addAll(next);
        return statuses;
    }

}
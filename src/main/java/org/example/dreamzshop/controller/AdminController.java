package org.example.dreamzshop.controller;

import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.PaymentStatus;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.repository.OrderRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class AdminController {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;


    public AdminController(
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        // =====================================================
        // REVENUE
        // =====================================================

        BigDecimal totalRevenue =
                orderRepository.calculateRevenue(
                        PaymentStatus.SUCCESS
                );

        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }


        // =====================================================
        // ORDER STATISTICS
        // =====================================================

        long totalOrders =
                orderRepository.count();

        long pendingOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.PENDING
                );

        long confirmedOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.CONFIRMED
                );

        long processingOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.PROCESSING
                );

        long packedOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.PACKED
                );

        long shippedOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.SHIPPED
                );

        long outForDeliveryOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.OUT_FOR_DELIVERY
                );

        long deliveredOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.DELIVERED
                );

        long cancelledOrders =
                orderRepository.countByOrderStatus(
                        OrderStatus.CANCELLED
                );


        // =====================================================
        // CUSTOMER STATISTICS
        // =====================================================

        long totalCustomers =
                userRepository.countByRole(
                        Role.CUSTOMER
                );

        long activeCustomers =
                userRepository.countByRoleAndEnabledTrue(
                        Role.CUSTOMER
                );

        long inactiveCustomers =
                userRepository.countByRoleAndEnabledFalse(
                        Role.CUSTOMER
                );


        // =====================================================
        // PRODUCT STATISTICS
        // =====================================================

        long totalProducts =
                productRepository.count();

        long activeProducts =
                productRepository.countByStatus(
                        org.example.dreamzshop.enums.ProductStatus.ACTIVE
                );

        long inactiveProducts =
                productRepository.countByStatus(
                        org.example.dreamzshop.enums.ProductStatus.INACTIVE
                );

        long lowStockProducts =
                productRepository.countLowStockProducts();

        long outOfStockProducts =
                productRepository.countOutOfStockProducts();


        // =====================================================
        // RECENT ORDERS
        // =====================================================

        List<org.example.dreamzshop.entity.Order> recentOrders =
                orderRepository
                        .findTop5ByOrderByCreatedAtDesc(
                                PageRequest.of(0, 5)
                        )
                        .getContent();


        // =====================================================
        // RECENT CUSTOMERS
        // =====================================================

        List<User> recentCustomers =
                userRepository
                        .findTop5ByRoleOrderByCreatedAtDesc(
                                Role.CUSTOMER,
                                PageRequest.of(0, 5)
                        )
                        .getContent();


        // =====================================================
        // LOW STOCK PRODUCTS
        // =====================================================

        List<Product> lowStockProductList =
                productRepository
                        .findLowStockProducts(
                                PageRequest.of(0, 5)
                        )
                        .getContent();


        // =====================================================
        // ADD DATA TO MODEL
        // =====================================================

        model.addAttribute(
                "totalRevenue",
                totalRevenue
        );

        model.addAttribute(
                "totalOrders",
                totalOrders
        );

        model.addAttribute(
                "pendingOrders",
                pendingOrders
        );

        model.addAttribute(
                "confirmedOrders",
                confirmedOrders
        );

        model.addAttribute(
                "processingOrders",
                processingOrders
        );

        model.addAttribute(
                "packedOrders",
                packedOrders
        );

        model.addAttribute(
                "shippedOrders",
                shippedOrders
        );

        model.addAttribute(
                "outForDeliveryOrders",
                outForDeliveryOrders
        );

        model.addAttribute(
                "deliveredOrders",
                deliveredOrders
        );

        model.addAttribute(
                "cancelledOrders",
                cancelledOrders
        );


        model.addAttribute(
                "totalCustomers",
                totalCustomers
        );

        model.addAttribute(
                "activeCustomers",
                activeCustomers
        );

        model.addAttribute(
                "inactiveCustomers",
                inactiveCustomers
        );


        model.addAttribute(
                "totalProducts",
                totalProducts
        );

        model.addAttribute(
                "activeProducts",
                activeProducts
        );

        model.addAttribute(
                "inactiveProducts",
                inactiveProducts
        );

        model.addAttribute(
                "lowStockProducts",
                lowStockProducts
        );

        model.addAttribute(
                "outOfStockProducts",
                outOfStockProducts
        );


        model.addAttribute(
                "recentOrders",
                recentOrders
        );

        model.addAttribute(
                "recentCustomers",
                recentCustomers
        );

        model.addAttribute(
                "lowStockProductList",
                lowStockProductList
        );


        return "admin/dashboard";
    }
}
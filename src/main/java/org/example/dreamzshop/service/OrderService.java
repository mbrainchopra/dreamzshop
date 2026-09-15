package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Order createCodOrder(
            String email,
            Long addressId,
            String couponCode
    );

    Order getCustomerOrder(
            String email,
            Long orderId
    );

    Page<Order> getCustomerOrders(
            String email,
            Pageable pageable
    );

    Page<Order> getAllOrders(
            Pageable pageable
    );

    Page<Order> getOrdersByStatus(
            OrderStatus status,
            Pageable pageable
    );

    Order getOrderById(
            Long orderId
    );

    void updateOrderStatus(
            Long orderId,
            OrderStatus newStatus
    );

    void cancelCustomerOrder(
            String email,
            Long orderId
    );
}
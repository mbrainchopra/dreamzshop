package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    List<OrderItem> findByOrderId(
            Long orderId
    );

    List<OrderItem> findByProductId(
            Long productId
    );

    long countByProductId(
            Long productId
    );


    // =========================================================
    // BEST SELLING PRODUCTS
    // =========================================================

    @Query("""
            SELECT oi.product.id,
                   oi.productName,
                   oi.productSku,
                   SUM(oi.quantity),
                   SUM(oi.subtotal)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.orderStatus = org.example.dreamzshop.enums.OrderStatus.DELIVERED
            GROUP BY oi.product.id,
                     oi.productName,
                     oi.productSku
            ORDER BY SUM(oi.quantity) DESC
            """)
    List<Object[]> findBestSellingProducts(
            Pageable pageable
    );


    // =========================================================
    // TOTAL QUANTITY SOLD
    // =========================================================

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.orderStatus = org.example.dreamzshop.enums.OrderStatus.DELIVERED
            """)
    Long getTotalQuantitySold();


    // =========================================================
    // TOTAL SALES FROM ORDER ITEMS
    // =========================================================

    @Query("""
            SELECT COALESCE(SUM(oi.subtotal), 0)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.orderStatus = org.example.dreamzshop.enums.OrderStatus.DELIVERED
            """)
    BigDecimal getTotalSales();


    // =========================================================
    // PRODUCT SALES
    // =========================================================

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE oi.product.id = :productId
              AND o.orderStatus = org.example.dreamzshop.enums.OrderStatus.DELIVERED
            """)
    Long getDeliveredQuantityByProductId(
            @Param("productId") Long productId
    );


    @Query("""
            SELECT COALESCE(SUM(oi.subtotal), 0)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE oi.product.id = :productId
              AND o.orderStatus = org.example.dreamzshop.enums.OrderStatus.DELIVERED
            """)
    BigDecimal getDeliveredSalesByProductId(
            @Param("productId") Long productId
    );
}
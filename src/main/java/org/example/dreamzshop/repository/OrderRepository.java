package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Order;
import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    Optional<Order> findByOrderNumber(
            String orderNumber
    );

    boolean existsByOrderNumber(
            String orderNumber
    );

    Optional<Order> findByIdAndUserId(
            Long id,
            Long userId
    );


    // =========================================================
    // CUSTOMER ORDERS
    // =========================================================

    Page<Order> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );


    // =========================================================
    // STATUS
    // =========================================================

    Page<Order> findByOrderStatus(
            OrderStatus orderStatus,
            Pageable pageable
    );

    Page<Order> findByPaymentStatus(
            PaymentStatus paymentStatus,
            Pageable pageable
    );

    long countByOrderStatus(
            OrderStatus orderStatus
    );


    // =========================================================
    // USER ORDER CHECK
    // =========================================================

    boolean existsByUserId(
            Long userId
    );


    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    Page<Order> findTop5ByOrderByCreatedAtDesc(
            Pageable pageable
    );


    @Query("""
            SELECT COALESCE(SUM(o.grandTotal), 0)
            FROM Order o
            WHERE o.paymentStatus = :paymentStatus
            """)
    BigDecimal calculateRevenue(
            @Param("paymentStatus") PaymentStatus paymentStatus
    );


    // =========================================================
    // REPORTS & ANALYTICS
    // =========================================================

    /*
     * Total revenue between two dates.
     *
     * Only successfully paid orders are counted.
     */
    @Query("""
            SELECT COALESCE(SUM(o.grandTotal), 0)
            FROM Order o
            WHERE o.paymentStatus = :paymentStatus
              AND o.createdAt >= :startDate
              AND o.createdAt < :endDate
            """)
    BigDecimal calculateRevenueBetweenDates(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    /*
     * Number of orders between two dates.
     */
    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.createdAt >= :startDate
              AND o.createdAt < :endDate
            """)
    long countOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    /*
     * Number of delivered orders between two dates.
     */
    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.orderStatus = :orderStatus
              AND o.createdAt >= :startDate
              AND o.createdAt < :endDate
            """)
    long countOrdersByStatusBetweenDates(
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    /*
     * Revenue grouped by order status.
     */
    @Query("""
            SELECT o.orderStatus,
                   COUNT(o),
                   COALESCE(SUM(o.grandTotal), 0)
            FROM Order o
            GROUP BY o.orderStatus
            ORDER BY COUNT(o) DESC
            """)
    List<Object[]> getOrderStatusReport();


    /*
     * Payment status summary.
     */
    @Query("""
            SELECT o.paymentStatus,
                   COUNT(o),
                   COALESCE(SUM(o.grandTotal), 0)
            FROM Order o
            GROUP BY o.paymentStatus
            ORDER BY COUNT(o) DESC
            """)
    List<Object[]> getPaymentStatusReport();


    /*
     * Monthly sales report.
     *
     * Uses MySQL YEAR() and MONTH() functions.
     * The project database is MySQL, so this is intentional.
     */
    @Query(value = """
            SELECT YEAR(created_at),
                   MONTH(created_at),
                   COUNT(*),
                   COALESCE(SUM(grand_total), 0)
            FROM orders
            WHERE payment_status = :paymentStatus
            GROUP BY YEAR(created_at), MONTH(created_at)
            ORDER BY YEAR(created_at) DESC,
                     MONTH(created_at) DESC
            LIMIT 12
            """,
            nativeQuery = true)
    List<Object[]> getMonthlySalesReport(
            @Param("paymentStatus") String paymentStatus
    );


    /*
     * Daily sales report for a selected date range.
     */
    @Query(value = """
            SELECT DATE(created_at),
                   COUNT(*),
                   COALESCE(SUM(grand_total), 0)
            FROM orders
            WHERE payment_status = :paymentStatus
              AND created_at >= :startDate
              AND created_at < :endDate
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at) ASC
            """,
            nativeQuery = true)
    List<Object[]> getDailySalesReport(
            @Param("paymentStatus") String paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
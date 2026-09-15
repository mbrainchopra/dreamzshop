package org.example.dreamzshop.service.impl;

import org.example.dreamzshop.enums.OrderStatus;
import org.example.dreamzshop.enums.PaymentStatus;
import org.example.dreamzshop.repository.OrderItemRepository;
import org.example.dreamzshop.repository.OrderRepository;
import org.example.dreamzshop.service.ReportsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportsServiceImpl
        implements ReportsService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;


    public ReportsServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository
    ) {
        this.orderRepository =
                orderRepository;

        this.orderItemRepository =
                orderItemRepository;
    }


    // =========================================================
    // TOTAL REVENUE
    // =========================================================

    @Override
    public BigDecimal getTotalRevenue() {

        BigDecimal revenue =
                orderRepository.calculateRevenue(
                        PaymentStatus.SUCCESS
                );

        return revenue != null
                ? revenue
                : BigDecimal.ZERO;
    }


    // =========================================================
    // TOTAL ORDERS
    // =========================================================

    @Override
    public long getTotalOrders() {

        return orderRepository.count();
    }


    // =========================================================
    // DELIVERED ORDERS
    // =========================================================

    @Override
    public long getDeliveredOrders() {

        return orderRepository.countByOrderStatus(
                OrderStatus.DELIVERED
        );
    }


    // =========================================================
    // AVERAGE ORDER VALUE
    // =========================================================

    @Override
    public BigDecimal getAverageOrderValue() {

        long deliveredOrders =
                getDeliveredOrders();

        if (deliveredOrders == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal revenue =
                getTotalRevenue();

        return revenue.divide(
                BigDecimal.valueOf(deliveredOrders),
                2,
                java.math.RoundingMode.HALF_UP
        );
    }


    // =========================================================
    // TOTAL QUANTITY SOLD
    // =========================================================

    @Override
    public Long getTotalQuantitySold() {

        Long quantity =
                orderItemRepository
                        .getTotalQuantitySold();

        return quantity != null
                ? quantity
                : 0L;
    }


    // =========================================================
    // TOTAL PRODUCT SALES
    // =========================================================

    @Override
    public BigDecimal getTotalProductSales() {

        BigDecimal sales =
                orderItemRepository
                        .getTotalSales();

        return sales != null
                ? sales
                : BigDecimal.ZERO;
    }


    // =========================================================
    // BEST SELLING PRODUCTS
    // =========================================================

    @Override
    public List<Object[]> getBestSellingProducts() {

        return orderItemRepository
                .findBestSellingProducts(
                        PageRequest.of(0, 10)
                );
    }


    // =========================================================
    // ORDER STATUS REPORT
    // =========================================================

    @Override
    public List<Object[]> getOrderStatusReport() {

        return orderRepository
                .getOrderStatusReport();
    }


    // =========================================================
    // PAYMENT STATUS REPORT
    // =========================================================

    @Override
    public List<Object[]> getPaymentStatusReport() {

        return orderRepository
                .getPaymentStatusReport();
    }


    // =========================================================
    // MONTHLY SALES REPORT
    // =========================================================

    @Override
    public List<Object[]> getMonthlySalesReport() {

        return orderRepository
                .getMonthlySalesReport(
                        PaymentStatus.SUCCESS.name()
                );
    }


    // =========================================================
    // DAILY SALES REPORT
    // =========================================================

    @Override
    public List<Object[]> getDailySalesReport(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null) {
            startDate =
                    LocalDate.now()
                            .minusDays(30);
        }

        if (endDate == null) {
            endDate =
                    LocalDate.now();
        }

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate.plusDays(1)
                        .atStartOfDay();

        return orderRepository
                .getDailySalesReport(
                        PaymentStatus.SUCCESS.name(),
                        startDateTime,
                        endDateTime
                );
    }
}
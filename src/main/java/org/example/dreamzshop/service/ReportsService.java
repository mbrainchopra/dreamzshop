package org.example.dreamzshop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReportsService {

    BigDecimal getTotalRevenue();

    long getTotalOrders();

    long getDeliveredOrders();

    BigDecimal getAverageOrderValue();

    Long getTotalQuantitySold();

    BigDecimal getTotalProductSales();

    List<Object[]> getBestSellingProducts();

    List<Object[]> getOrderStatusReport();

    List<Object[]> getPaymentStatusReport();

    List<Object[]> getMonthlySalesReport();

    List<Object[]> getDailySalesReport(
            LocalDate startDate,
            LocalDate endDate
    );
}
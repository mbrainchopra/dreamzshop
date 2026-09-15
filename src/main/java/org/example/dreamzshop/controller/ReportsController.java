package org.example.dreamzshop.controller;

import org.example.dreamzshop.service.ReportsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
public class ReportsController {

    private final ReportsService reportsService;


    public ReportsController(
            ReportsService reportsService
    ) {
        this.reportsService =
                reportsService;
    }


    // =========================================================
    // REPORTS DASHBOARD
    // =========================================================

    @GetMapping
    public String reports(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate endDate,

            Model model
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


        // =====================================================
        // SUMMARY
        // =====================================================

        model.addAttribute(
                "totalRevenue",
                reportsService.getTotalRevenue()
        );

        model.addAttribute(
                "totalOrders",
                reportsService.getTotalOrders()
        );

        model.addAttribute(
                "deliveredOrders",
                reportsService.getDeliveredOrders()
        );

        model.addAttribute(
                "averageOrderValue",
                reportsService.getAverageOrderValue()
        );

        model.addAttribute(
                "totalQuantitySold",
                reportsService.getTotalQuantitySold()
        );


        // =====================================================
        // BEST SELLERS
        // =====================================================

        model.addAttribute(
                "bestSellingProducts",
                reportsService
                        .getBestSellingProducts()
        );


        // =====================================================
        // STATUS REPORTS
        // =====================================================

        model.addAttribute(
                "orderStatusReport",
                reportsService
                        .getOrderStatusReport()
        );

        model.addAttribute(
                "paymentStatusReport",
                reportsService
                        .getPaymentStatusReport()
        );


        // =====================================================
        // SALES REPORTS
        // =====================================================

        model.addAttribute(
                "monthlySalesReport",
                reportsService
                        .getMonthlySalesReport()
        );

        model.addAttribute(
                "dailySalesReport",
                reportsService
                        .getDailySalesReport(
                                startDate,
                                endDate
                        )
        );


        // =====================================================
        // SELECTED DATE RANGE
        // =====================================================

        model.addAttribute(
                "startDate",
                startDate
        );

        model.addAttribute(
                "endDate",
                endDate
        );


        return "admin/reports";
    }
}
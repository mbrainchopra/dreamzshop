package org.example.dreamzshop.controller;

import org.example.dreamzshop.service.ReportsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    // =========================================================
    // REPORTS DASHBOARD
    // URL: /admin/reports
    // =========================================================

    @GetMapping
    public String reports(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            Model model,
            RedirectAttributes redirectAttributes
    ) {

        // =====================================================
        // DEFAULT DATE RANGE
        // =====================================================

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // =====================================================
        // VALIDATE DATE RANGE
        // =====================================================

        if (startDate.isAfter(endDate)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Start date cannot be after end date."
            );

            return "redirect:/admin/reports";
        }

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
        // BEST SELLING PRODUCTS
        // =====================================================

        model.addAttribute(
                "bestSellingProducts",
                reportsService.getBestSellingProducts()
        );

        // =====================================================
        // ORDER STATUS REPORT
        // =====================================================

        model.addAttribute(
                "orderStatusReport",
                reportsService.getOrderStatusReport()
        );

        // =====================================================
        // PAYMENT STATUS REPORT
        // =====================================================

        model.addAttribute(
                "paymentStatusReport",
                reportsService.getPaymentStatusReport()
        );

        // =====================================================
        // MONTHLY SALES
        // =====================================================

        model.addAttribute(
                "monthlySalesReport",
                reportsService.getMonthlySalesReport()
        );

        // =====================================================
        // DAILY SALES
        // =====================================================

        model.addAttribute(
                "dailySalesReport",
                reportsService.getDailySalesReport(
                        startDate,
                        endDate
                )
        );

        // =====================================================
        // PAGE
        // =====================================================

        return "admin/reports";
    }
}
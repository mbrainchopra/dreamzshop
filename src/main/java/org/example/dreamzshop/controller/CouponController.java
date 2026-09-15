package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import org.example.dreamzshop.entity.Coupon;
import org.example.dreamzshop.enums.DiscountType;
import org.example.dreamzshop.service.CouponService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(
            CouponService couponService
    ) {

        this.couponService =
                couponService;
    }

    @GetMapping
    public String coupons(
            Model model,
            @RequestParam(defaultValue = "0") int page
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

        Page<Coupon> coupons =
                couponService.getAllCoupons(
                        pageable
                );

        model.addAttribute(
                "coupons",
                coupons
        );

        return "admin/coupons";
    }

    @GetMapping("/add")
    public String addCoupon(
            Model model
    ) {

        model.addAttribute(
                "coupon",
                new Coupon()
        );

        model.addAttribute(
                "discountTypes",
                DiscountType.values()
        );

        return "admin/coupon-form";
    }

    @PostMapping("/save")
    public String saveCoupon(
            @Valid @ModelAttribute("coupon") Coupon coupon,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "discountTypes",
                    DiscountType.values()
            );

            return "admin/coupon-form";
        }

        try {

            couponService.saveCoupon(
                    coupon
            );

            return "redirect:/admin/coupons?saved=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "discountTypes",
                    DiscountType.values()
            );

            return "admin/coupon-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editCoupon(
            @PathVariable Long id,
            Model model
    ) {

        try {

            Coupon coupon =
                    couponService.getCoupon(
                            id
                    );

            model.addAttribute(
                    "coupon",
                    coupon
            );

            model.addAttribute(
                    "discountTypes",
                    DiscountType.values()
            );

            return "admin/coupon-form";

        } catch (IllegalArgumentException e) {

            return "redirect:/admin/coupons?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/update/{id}")
    public String updateCoupon(
            @PathVariable Long id,
            @Valid @ModelAttribute("coupon") Coupon coupon,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "discountTypes",
                    DiscountType.values()
            );

            return "admin/coupon-form";
        }

        try {

            couponService.updateCoupon(
                    id,
                    coupon
            );

            return "redirect:/admin/coupons?updated=true";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "discountTypes",
                    DiscountType.values()
            );

            return "admin/coupon-form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleCoupon(
            @PathVariable Long id
    ) {

        try {

            couponService.toggleCoupon(
                    id
            );

        } catch (IllegalArgumentException ignored) {
        }

        return "redirect:/admin/coupons";
    }

    @PostMapping("/delete/{id}")
    public String deleteCoupon(
            @PathVariable Long id
    ) {

        try {

            couponService.deleteCoupon(
                    id
            );

            return "redirect:/admin/coupons?deleted=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/admin/coupons?error="
                    + encode(e.getMessage());
        }
    }

    private String encode(
            String message
    ) {

        if (message == null) {
            return "Something went wrong";
        }

        return message
                .replace(" ", "%20")
                .replace("?", "%3F")
                .replace("&", "%26");
    }
}
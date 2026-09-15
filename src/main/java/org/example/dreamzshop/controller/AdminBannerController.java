package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Banner;
import org.example.dreamzshop.service.BannerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/banners")
public class AdminBannerController {

    private final BannerService bannerService;


    // =========================================================
    // BANNER LIST
    // =========================================================

    @GetMapping
    public String listBanners(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 50) {
            size = 10;
        }

        Page<Banner> banners =
                bannerService.getAllBanners(
                        keyword,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "displayOrder"
                                )
                        )
                );

        model.addAttribute(
                "banners",
                banners
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "currentPage",
                page
        );

        model.addAttribute(
                "pageSize",
                size
        );

        return "admin/banners";
    }


    // =========================================================
    // ADD BANNER
    // =========================================================

    @GetMapping("/add")
    public String addBanner(
            Model model) {

        Banner banner =
                Banner.builder()
                        .title("")
                        .subtitle("")
                        .buttonText("Shop Now")
                        .buttonUrl("/products")
                        .imageUrl("")
                        .displayOrder(0)
                        .enabled(true)
                        .build();

        model.addAttribute(
                "banner",
                banner
        );

        return "admin/banner-form";
    }


    // =========================================================
    // SAVE BANNER
    // =========================================================

    @PostMapping("/save")
    public String saveBanner(
            @Valid @ModelAttribute("banner") Banner banner,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "admin/banner-form";
        }

        try {

            bannerService.saveBanner(
                    banner
            );

            return "redirect:/admin/banners?success=Banner+created+successfully";

        } catch (IllegalArgumentException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "admin/banner-form";
        }
    }


    // =========================================================
    // EDIT BANNER
    // =========================================================

    @GetMapping("/edit/{id}")
    public String editBanner(
            @PathVariable Long id,
            Model model) {

        Banner banner =
                bannerService.getBanner(id);

        model.addAttribute(
                "banner",
                banner
        );

        return "admin/banner-form";
    }


    // =========================================================
    // UPDATE BANNER
    // =========================================================

    @PostMapping("/update/{id}")
    public String updateBanner(
            @PathVariable Long id,
            @Valid @ModelAttribute("banner") Banner banner,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {

            return "admin/banner-form";
        }

        try {

            bannerService.updateBanner(
                    id,
                    banner
            );

            return "redirect:/admin/banners?success=Banner+updated+successfully";

        } catch (IllegalArgumentException ex) {

            model.addAttribute(
                    "error",
                    ex.getMessage()
            );

            return "admin/banner-form";
        }
    }


    // =========================================================
    // TOGGLE BANNER
    // =========================================================

    @PostMapping("/toggle/{id}")
    public String toggleBanner(
            @PathVariable Long id) {

        bannerService.toggleBanner(id);

        return "redirect:/admin/banners?success=Banner+status+updated";
    }


    // =========================================================
    // DELETE BANNER
    // =========================================================

    @PostMapping("/delete/{id}")
    public String deleteBanner(
            @PathVariable Long id) {

        bannerService.deleteBanner(id);

        return "redirect:/admin/banners?success=Banner+deleted+successfully";
    }
}
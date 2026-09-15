package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Brand;
import org.example.dreamzshop.repository.BrandRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/brands")
public class BrandController {

    private final BrandRepository brandRepository;

    // ===============================
    // LIST BRANDS
    // ===============================

    @GetMapping
    public String listBrands(Model model) {

        model.addAttribute(
                "brands",
                brandRepository.findByOrderByNameAsc()
        );

        return "admin/brands";
    }

    // ===============================
    // ADD FORM
    // ===============================

    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute("brand", new Brand());

        return "admin/brand-form";
    }

    // ===============================
    // SAVE BRAND
    // ===============================

    @PostMapping("/save")
    public String saveBrand(
            @Valid @ModelAttribute("brand") Brand brand,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "admin/brand-form";
        }

        if (brandRepository.existsByNameIgnoreCase(
                brand.getName())) {

            model.addAttribute(
                    "nameError",
                    "Brand name already exists."
            );

            return "admin/brand-form";
        }

        brandRepository.save(brand);

        return "redirect:/admin/brands";
    }

    // ===============================
    // EDIT FORM
    // ===============================

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid brand ID: " + id
                        )
                );

        model.addAttribute("brand", brand);

        return "admin/brand-form";
    }

    // ===============================
    // UPDATE BRAND
    // ===============================

    @PostMapping("/update/{id}")
    public String updateBrand(
            @PathVariable Long id,
            @Valid @ModelAttribute("brand") Brand brand,
            BindingResult bindingResult,
            Model model) {

        Brand existingBrand =
                brandRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid brand ID: " + id
                                )
                        );

        if (bindingResult.hasErrors()) {
            return "admin/brand-form";
        }

        boolean duplicate =
                !existingBrand.getName()
                        .equalsIgnoreCase(brand.getName())
                        &&
                        brandRepository.existsByNameIgnoreCase(
                                brand.getName()
                        );

        if (duplicate) {

            model.addAttribute(
                    "nameError",
                    "Brand name already exists."
            );

            return "admin/brand-form";
        }

        existingBrand.setName(brand.getName());
        existingBrand.setDescription(brand.getDescription());
        existingBrand.setLogo(brand.getLogo());
        existingBrand.setEnabled(brand.isEnabled());
        existingBrand.setUpdatedAt(LocalDateTime.now());

        brandRepository.save(existingBrand);

        return "redirect:/admin/brands";
    }

    // ===============================
    // ENABLE / DISABLE
    // ===============================

    @PostMapping("/toggle/{id}")
    public String toggleBrand(
            @PathVariable Long id) {

        Brand brand =
                brandRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid brand ID: " + id
                                )
                        );

        brand.setEnabled(!brand.isEnabled());

        brandRepository.save(brand);

        return "redirect:/admin/brands";
    }

    // ===============================
    // DELETE BRAND
    // ===============================

    @PostMapping("/delete/{id}")
    public String deleteBrand(
            @PathVariable Long id) {

        Brand brand =
                brandRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid brand ID: " + id
                                )
                        );

        brandRepository.delete(brand);

        return "redirect:/admin/brands";
    }
}
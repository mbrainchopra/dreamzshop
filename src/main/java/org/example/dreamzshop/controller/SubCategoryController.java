package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Category;
import org.example.dreamzshop.entity.SubCategory;
import org.example.dreamzshop.repository.CategoryRepository;
import org.example.dreamzshop.repository.SubCategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/subcategories")
public class SubCategoryController {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;

    // ===============================
    // LIST SUBCATEGORIES
    // ===============================

    @GetMapping
    public String listSubCategories(Model model) {

        model.addAttribute(
                "subCategories",
                subCategoryRepository.findByOrderByNameAsc()
        );

        return "admin/subcategories";
    }

    // ===============================
    // ADD FORM
    // ===============================

    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute("subCategory", new SubCategory());

        model.addAttribute(
                "categories",
                categoryRepository.findByEnabledTrueOrderByNameAsc()
        );

        return "admin/subcategory-form";
    }

    // ===============================
    // SAVE
    // ===============================

    @PostMapping("/save")
    public String saveSubCategory(
            @Valid @ModelAttribute("subCategory")
            SubCategory subCategory,
            BindingResult bindingResult,
            @RequestParam("categoryId") Long categoryId,
            Model model) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid category ID: " + categoryId
                        )
                );

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "categories",
                    categoryRepository.findByEnabledTrueOrderByNameAsc()
            );

            return "admin/subcategory-form";
        }

        if (subCategoryRepository
                .existsByCategoryIdAndNameIgnoreCase(
                        categoryId,
                        subCategory.getName()
                )) {

            model.addAttribute(
                    "nameError",
                    "This subcategory already exists under the selected category."
            );

            model.addAttribute(
                    "categories",
                    categoryRepository.findByEnabledTrueOrderByNameAsc()
            );

            return "admin/subcategory-form";
        }

        subCategory.setCategory(category);

        subCategoryRepository.save(subCategory);

        return "redirect:/admin/subcategories";
    }

    // ===============================
    // EDIT FORM
    // ===============================

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        SubCategory subCategory =
                subCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid subcategory ID: " + id
                                )
                        );

        model.addAttribute(
                "subCategory",
                subCategory
        );

        model.addAttribute(
                "categories",
                categoryRepository.findByEnabledTrueOrderByNameAsc()
        );

        return "admin/subcategory-form";
    }

    // ===============================
    // UPDATE
    // ===============================

    @PostMapping("/update/{id}")
    public String updateSubCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("subCategory")
            SubCategory subCategory,
            BindingResult bindingResult,
            @RequestParam("categoryId") Long categoryId,
            Model model) {

        SubCategory existing =
                subCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid subcategory ID: " + id
                                )
                        );

        Category category =
                categoryRepository.findById(categoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid category ID: " + categoryId
                                )
                        );

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "categories",
                    categoryRepository.findByEnabledTrueOrderByNameAsc()
            );

            return "admin/subcategory-form";
        }

        boolean duplicate =
                !existing.getName().equalsIgnoreCase(
                        subCategory.getName()
                )
                        &&
                        subCategoryRepository
                                .existsByCategoryIdAndNameIgnoreCase(
                                        categoryId,
                                        subCategory.getName()
                                );

        if (duplicate) {

            model.addAttribute(
                    "nameError",
                    "This subcategory already exists under the selected category."
            );

            model.addAttribute(
                    "categories",
                    categoryRepository.findByEnabledTrueOrderByNameAsc()
            );

            return "admin/subcategory-form";
        }

        existing.setName(subCategory.getName());
        existing.setDescription(subCategory.getDescription());
        existing.setImage(subCategory.getImage());
        existing.setEnabled(subCategory.isEnabled());
        existing.setCategory(category);
        existing.setUpdatedAt(
                java.time.LocalDateTime.now()
        );

        subCategoryRepository.save(existing);

        return "redirect:/admin/subcategories";
    }

    // ===============================
    // ENABLE / DISABLE
    // ===============================

    @PostMapping("/toggle/{id}")
    public String toggleSubCategory(
            @PathVariable Long id) {

        SubCategory subCategory =
                subCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid subcategory ID: " + id
                                )
                        );

        subCategory.setEnabled(
                !subCategory.isEnabled()
        );

        subCategoryRepository.save(subCategory);

        return "redirect:/admin/subcategories";
    }

    // ===============================
    // DELETE
    // ===============================

    @PostMapping("/delete/{id}")
    public String deleteSubCategory(
            @PathVariable Long id) {

        SubCategory subCategory =
                subCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid subcategory ID: " + id
                                )
                        );

        subCategoryRepository.delete(subCategory);

        return "redirect:/admin/subcategories";
    }
}
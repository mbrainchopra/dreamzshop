package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Category;
import org.example.dreamzshop.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    // ===============================
    // LIST CATEGORIES
    // ===============================

    @GetMapping
    public String listCategories(Model model) {

        model.addAttribute(
                "categories",
                categoryRepository.findAll()
        );

        return "admin/categories";
    }

    // ===============================
    // SHOW ADD FORM
    // ===============================

    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute("category", new Category());

        return "admin/category-form";
    }

    // ===============================
    // SAVE CATEGORY
    // ===============================

    @PostMapping("/save")
    public String saveCategory(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "admin/category-form";
        }

        if (categoryRepository.existsByNameIgnoreCase(
                category.getName())) {

            model.addAttribute(
                    "nameError",
                    "Category name already exists."
            );

            return "admin/category-form";
        }

        categoryRepository.save(category);

        return "redirect:/admin/categories";
    }

    // ===============================
    // SHOW EDIT FORM
    // ===============================

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid category ID: " + id
                        )
                );

        model.addAttribute("category", category);

        return "admin/category-form";
    }

    // ===============================
    // UPDATE CATEGORY
    // ===============================

    @PostMapping("/update/{id}")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "admin/category-form";
        }

        Category existingCategory =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid category ID: " + id
                                )
                        );

        if (!existingCategory.getName()
                .equalsIgnoreCase(category.getName())
                && categoryRepository.existsByNameIgnoreCase(
                category.getName())) {

            model.addAttribute(
                    "nameError",
                    "Category name already exists."
            );

            return "admin/category-form";
        }

        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setImage(category.getImage());
        existingCategory.setEnabled(category.isEnabled());
        existingCategory.setUpdatedAt(
                java.time.LocalDateTime.now()
        );

        categoryRepository.save(existingCategory);

        return "redirect:/admin/categories";
    }

    // ===============================
    // ENABLE / DISABLE
    // ===============================

    @PostMapping("/toggle/{id}")
    public String toggleCategory(@PathVariable Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid category ID: " + id
                                )
                        );

        category.setEnabled(!category.isEnabled());

        categoryRepository.save(category);

        return "redirect:/admin/categories";
    }

    // ===============================
    // DELETE
    // ===============================

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid category ID: " + id
                                )
                        );

        categoryRepository.delete(category);

        return "redirect:/admin/categories";
    }
}
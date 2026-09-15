package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.ProductImage;
import org.example.dreamzshop.repository.ProductImageRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    // =========================================================
    // IMAGE MANAGEMENT PAGE
    // =========================================================

    @GetMapping("/admin/products/{productId}/images")
    public String manageImages(
            @PathVariable Long productId,
            Model model) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"
                        )
                );

        List<ProductImage> images =
                productImageRepository
                        .findByProductIdAndEnabledTrueOrderBySortOrderAsc(
                                productId
                        );

        model.addAttribute("product", product);
        model.addAttribute("images", images);

        return "admin/product-images";
    }

    // =========================================================
    // ADD IMAGE
    // =========================================================

    @PostMapping("/admin/products/{productId}/images/save")
    public String saveImage(
            @PathVariable Long productId,
            @RequestParam String imageUrl,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "0") Integer sortOrder,
            @RequestParam(defaultValue = "false") boolean primaryImage) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"
                        )
                );

        // -----------------------------------------------------
        // If this is primary image, remove primary flag
        // from existing images.
        // -----------------------------------------------------

        if (primaryImage) {

            ProductImage existingPrimary =
                    productImageRepository
                            .findByProductIdAndPrimaryImageTrue(
                                    productId
                            )
                            .orElse(null);

            if (existingPrimary != null) {

                existingPrimary.setPrimaryImage(false);
                existingPrimary.setUpdatedAt(
                        LocalDateTime.now()
                );

                productImageRepository.save(existingPrimary);
            }

            product.setMainImage(imageUrl);
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .altText(altText)
                .primaryImage(primaryImage)
                .sortOrder(sortOrder)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productImageRepository.save(image);

        if (primaryImage) {
            productRepository.save(product);
        }

        return "redirect:/admin/products/"
                + productId
                + "/images?success=Image+added+successfully";
    }

    // =========================================================
    // SET PRIMARY IMAGE
    // =========================================================

    @PostMapping(
            "/admin/products/{productId}/images/{imageId}/primary"
    )
    public String setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"
                        )
                );

        ProductImage image =
                productImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Image not found"
                                )
                        );

        if (!image.getProduct().getId().equals(productId)) {

            throw new IllegalArgumentException(
                    "Image does not belong to this product"
            );
        }

        // Remove old primary image

        ProductImage oldPrimary =
                productImageRepository
                        .findByProductIdAndPrimaryImageTrue(
                                productId
                        )
                        .orElse(null);

        if (oldPrimary != null
                && !oldPrimary.getId().equals(imageId)) {

            oldPrimary.setPrimaryImage(false);
            oldPrimary.setUpdatedAt(
                    LocalDateTime.now()
            );

            productImageRepository.save(oldPrimary);
        }

        // Set new primary

        image.setPrimaryImage(true);
        image.setEnabled(true);
        image.setUpdatedAt(LocalDateTime.now());

        productImageRepository.save(image);

        // Sync Product main image

        product.setMainImage(image.getImageUrl());
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        return "redirect:/admin/products/"
                + productId
                + "/images?success=Primary+image+updated";
    }

    // =========================================================
    // TOGGLE IMAGE
    // =========================================================

    @PostMapping(
            "/admin/products/{productId}/images/{imageId}/toggle"
    )
    public String toggleImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        ProductImage image =
                productImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Image not found"
                                )
                        );

        if (!image.getProduct().getId().equals(productId)) {

            throw new IllegalArgumentException(
                    "Image does not belong to this product"
            );
        }

        image.setEnabled(!image.isEnabled());
        image.setUpdatedAt(LocalDateTime.now());

        productImageRepository.save(image);

        return "redirect:/admin/products/"
                + productId
                + "/images";
    }

    // =========================================================
    // UPDATE SORT ORDER
    // =========================================================

    @PostMapping(
            "/admin/products/{productId}/images/{imageId}/sort"
    )
    public String updateSortOrder(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestParam Integer sortOrder) {

        ProductImage image =
                productImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Image not found"
                                )
                        );

        if (!image.getProduct().getId().equals(productId)) {

            throw new IllegalArgumentException(
                    "Image does not belong to this product"
            );
        }

        if (sortOrder < 0) {
            sortOrder = 0;
        }

        image.setSortOrder(sortOrder);
        image.setUpdatedAt(LocalDateTime.now());

        productImageRepository.save(image);

        return "redirect:/admin/products/"
                + productId
                + "/images?success=Image+order+updated";
    }

    // =========================================================
    // DELETE IMAGE
    // =========================================================

    @PostMapping(
            "/admin/products/{productId}/images/{imageId}/delete"
    )
    public String deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found"
                                )
                        );

        ProductImage image =
                productImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Image not found"
                                )
                        );

        if (!image.getProduct().getId().equals(productId)) {

            throw new IllegalArgumentException(
                    "Image does not belong to this product"
            );
        }

        boolean wasPrimary = image.isPrimaryImage();

        productImageRepository.delete(image);

        // -----------------------------------------------------
        // If primary image was deleted, choose another image
        // as primary automatically.
        // -----------------------------------------------------

        if (wasPrimary) {

            List<ProductImage> remainingImages =
                    productImageRepository
                            .findByProductIdAndEnabledTrueOrderBySortOrderAsc(
                                    productId
                            );

            if (!remainingImages.isEmpty()) {

                ProductImage newPrimary =
                        remainingImages.get(0);

                newPrimary.setPrimaryImage(true);
                newPrimary.setUpdatedAt(
                        LocalDateTime.now()
                );

                productImageRepository.save(newPrimary);

                product.setMainImage(
                        newPrimary.getImageUrl()
                );

            } else {

                product.setMainImage(null);
            }

            product.setUpdatedAt(LocalDateTime.now());

            productRepository.save(product);
        }

        return "redirect:/admin/products/"
                + productId
                + "/images?success=Image+deleted";
    }
}
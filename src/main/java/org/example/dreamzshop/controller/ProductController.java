package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.ProductImage;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.repository.BrandRepository;
import org.example.dreamzshop.repository.CartItemRepository;
import org.example.dreamzshop.repository.OrderItemRepository;
import org.example.dreamzshop.repository.ReviewRepository;
import org.example.dreamzshop.repository.WishlistItemRepository;
import org.example.dreamzshop.repository.OfferRepository;
import org.example.dreamzshop.repository.InventoryTransactionRepository;
import org.example.dreamzshop.repository.CategoryRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.repository.SubCategoryRepository;
import org.example.dreamzshop.service.ReviewService;
import org.example.dreamzshop.util.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final SubCategoryRepository subCategoryRepository;

    private final BrandRepository brandRepository;

    private final ReviewService reviewService;

    private final CartItemRepository cartItemRepository;

    private final OrderItemRepository orderItemRepository;

    private final ReviewRepository reviewRepository;

    private final WishlistItemRepository wishlistItemRepository;

    private final OfferRepository offerRepository;

    private final InventoryTransactionRepository inventoryTransactionRepository;


    // =========================================================
    // ADMIN - PRODUCT LIST
    // =========================================================

    @GetMapping("/admin/products")
    public String products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 50) {
            size = 10;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<Product> products;

        if (keyword != null
                && !keyword.trim().isEmpty()) {

            products =
                    productRepository
                            .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                                    keyword.trim(),
                                    keyword.trim(),
                                    pageable
                            );

        } else if (status != null) {

            products =
                    productRepository.findByStatus(
                            status,
                            pageable
                    );

        } else {

            products =
                    productRepository.findAll(
                            pageable
                    );
        }

        model.addAttribute(
                "products",
                products
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "status",
                status
        );

        // Product status dropdown
        model.addAttribute(
                "statuses",
                ProductStatus.values()
        );

        return "admin/products";
    }


    // =========================================================
    // ADMIN - ADD PRODUCT
    // =========================================================

    @GetMapping("/admin/products/add")
    public String addProduct(
            Model model) {

        Product product =
                new Product();

        // Default status
        product.setStatus(
                ProductStatus.ACTIVE
        );

        product.setFeatured(false);

        product.setTaxable(true);

        model.addAttribute(
                "product",
                product
        );

        loadProductFormData(model);

        return "admin/product-form";
    }


    // =========================================================
    // ADMIN - SAVE PRODUCT
    // =========================================================

    @PostMapping("/admin/products/save")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam(
                    value = "imageUrls",
                    required = false
            ) List<String> imageUrls,
            Model model) {

        if (result.hasErrors()) {

            loadProductFormData(model);

            return "admin/product-form";
        }


        if (!validatePricing(
                product,
                result
        ) || !validateCategoryRelationship(
                product,
                result
        )) {

            loadProductFormData(model);

            return "admin/product-form";
        }


        // =====================================================
        // PRODUCT IMAGES
        // =====================================================

        addProductImages(
                product,
                imageUrls
        );


        // =====================================================
        // SAVE PRODUCT
        // =====================================================

        productRepository.save(product);

        return "redirect:/admin/products?success=Product+saved+successfully";
    }


    // =========================================================
    // ADMIN - EDIT PRODUCT
    // =========================================================

    @GetMapping("/admin/products/edit/{id}")
    public String editProduct(
            @PathVariable Long id,
            Model model) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found."
                                )
                        );

        model.addAttribute(
                "product",
                product
        );

        loadProductFormData(model);

        return "admin/product-form";
    }


    // =========================================================
    // ADMIN - UPDATE PRODUCT
    // =========================================================

    @PostMapping("/admin/products/update/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam(
                    value = "imageUrls",
                    required = false
            ) List<String> imageUrls,
            Model model) {

        if (result.hasErrors()) {

            loadProductFormData(model);

            return "admin/product-form";
        }


        if (!validatePricing(
                product,
                result
        ) || !validateCategoryRelationship(
                product,
                result
        )) {

            loadProductFormData(model);

            return "admin/product-form";
        }


        Product existingProduct =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found."
                                )
                        );


        // =====================================================
        // BASIC INFORMATION
        // =====================================================

        existingProduct.setName(
                product.getName()
        );

        existingProduct.setSku(
                product.getSku()
        );

        existingProduct.setBarcode(
                product.getBarcode()
        );

        existingProduct.setDescription(
                product.getDescription()
        );

        existingProduct.setShortDescription(
                product.getShortDescription()
        );


        // =====================================================
        // CATEGORY
        // =====================================================

        existingProduct.setCategory(
                product.getCategory()
        );

        existingProduct.setSubCategory(
                product.getSubCategory()
        );

        existingProduct.setBrand(
                product.getBrand()
        );


        // =====================================================
        // PRICING
        // =====================================================

        existingProduct.setMrp(
                product.getMrp()
        );

        existingProduct.setSellingPrice(
                product.getSellingPrice()
        );

        existingProduct.setCostPrice(
                product.getCostPrice()
        );

        existingProduct.setTaxPercentage(
                product.getTaxPercentage()
        );


        // =====================================================
        // INVENTORY
        // =====================================================

        existingProduct.setStockQuantity(
                product.getStockQuantity()
        );

        existingProduct.setMinimumStockLevel(
                product.getMinimumStockLevel()
        );


        // =====================================================
        // UNIT / WEIGHT
        // =====================================================

        existingProduct.setUnit(
                product.getUnit()
        );

        existingProduct.setWeight(
                product.getWeight()
        );

        existingProduct.setWeightUnit(
                product.getWeightUnit()
        );


        // =====================================================
        // PRODUCT SETTINGS
        // =====================================================

        existingProduct.setStatus(
                product.getStatus()
        );

        existingProduct.setFeatured(
                product.isFeatured()
        );

        existingProduct.setTaxable(
                product.isTaxable()
        );


        // =====================================================
        // SEO
        // =====================================================

        existingProduct.setMetaTitle(
                product.getMetaTitle()
        );

        existingProduct.setMetaDescription(
                product.getMetaDescription()
        );

        existingProduct.setSlug(
                product.getSlug()
        );


        // =====================================================
        // PRODUCT IMAGES
        // =====================================================

        updateProductImages(
                existingProduct,
                imageUrls
        );


        // =====================================================
        // SAVE
        // =====================================================

        productRepository.save(
                existingProduct
        );

        return "redirect:/admin/products?success=Product+updated+successfully";
    }


    // =========================================================
    // PRODUCT IMAGE - ADD
    // =========================================================

    private void addProductImages(
            Product product,
            List<String> imageUrls) {

        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        int sortOrder = 0;

        for (String url : imageUrls) {

            if (url == null || url.trim().isEmpty()) {
                continue;
            }

            String cleanUrl = url.trim();

            ProductImage image =
                    ProductImage.builder()
                            .product(product)
                            .imageUrl(cleanUrl)
                            .altText(product.getName())
                            .primaryImage(sortOrder == 0)
                            .sortOrder(sortOrder)
                            .enabled(true)
                            .build();

            product.addImage(image);

            sortOrder++;
        }

        // First image becomes main image
        if (!product.getImages().isEmpty()) {

            product.setMainImage(
                    product.getImages()
                            .get(0)
                            .getImageUrl()
            );
        }
    }


    // =========================================================
    // PRODUCT IMAGE - UPDATE
    // =========================================================

    private void updateProductImages(
            Product product,
            List<String> imageUrls) {

        // Remove existing images
        product.getImages().clear();

        int sortOrder = 0;

        if (imageUrls != null) {

            for (String url : imageUrls) {

                if (url == null || url.trim().isEmpty()) {
                    continue;
                }

                String cleanUrl = url.trim();

                ProductImage image =
                        ProductImage.builder()
                                .product(product)
                                .imageUrl(cleanUrl)
                                .altText(product.getName())
                                .primaryImage(sortOrder == 0)
                                .sortOrder(sortOrder)
                                .enabled(true)
                                .build();

                product.addImage(image);

                sortOrder++;
            }
        }


        // First image becomes main image
        if (!product.getImages().isEmpty()) {

            product.setMainImage(
                    product.getImages()
                            .get(0)
                            .getImageUrl()
            );

        } else {

            product.setMainImage(null);
        }
    }


    // =========================================================
    // ADMIN - DELETE PRODUCT
    // =========================================================

    @PostMapping("/admin/products/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id
    ) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found."
                                )
                        );

        long orderReferences =
                orderItemRepository.countByProductId(id);

        long cartReferences =
                cartItemRepository.countByProductId(id);

        long reviewReferences =
                reviewRepository.countByProductId(id);

        long wishlistReferences =
                wishlistItemRepository.countByProductId(id);

        long offerReferences =
                offerRepository.findByProductId(id).size();

        long inventoryReferences =
                inventoryTransactionRepository.countByProductId(id);


        if (orderReferences > 0
                || cartReferences > 0
                || reviewReferences > 0
                || wishlistReferences > 0
                || offerReferences > 0
                || inventoryReferences > 0) {

            // Historical references must remain readable.
            // Soft delete instead.

            product.setStatus(
                    ProductStatus.INACTIVE
            );

            product.setFeatured(false);

            productRepository.save(product);

            return "redirect:/admin/products?success=Product+has+existing+references+and+was+deactivated+instead+of+deleted";
        }


        productRepository.delete(product);

        return "redirect:/admin/products?success=Product+deleted+successfully";
    }


    // =========================================================
    // VALIDATE CATEGORY RELATIONSHIP
    // =========================================================

    private boolean validateCategoryRelationship(
            Product product,
            BindingResult result) {

        if (product.getCategory() == null
                || product.getCategory().getId() == null) {

            result.rejectValue(
                    "category",
                    "category.required",
                    "Category is required"
            );

            return false;
        }


        if (product.getSubCategory() == null
                || product.getSubCategory().getId() == null) {

            return true;
        }


        var subCategory =
                subCategoryRepository
                        .findById(
                                product.getSubCategory().getId()
                        )
                        .orElse(null);


        if (subCategory == null
                || subCategory.getCategory() == null
                || !product.getCategory()
                .getId()
                .equals(
                        subCategory
                                .getCategory()
                                .getId()
                )) {

            result.rejectValue(
                    "subCategory",
                    "subcategory.category.mismatch",
                    "Selected subcategory does not belong to the selected category"
            );

            return false;
        }


        // Replace detached relation
        // with managed entity.

        product.setSubCategory(
                subCategory
        );

        return true;
    }


    // =========================================================
    // PUBLIC - PRODUCT LIST
    // =========================================================

    @GetMapping("/products")
    public String productListing(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subCategoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 50) {
            size = 12;
        }


        Sort sorting;


        switch (sort.toLowerCase()) {

            case "price-low":

                sorting =
                        Sort.by(
                                Sort.Direction.ASC,
                                "sellingPrice"
                        );

                break;


            case "price-high":

                sorting =
                        Sort.by(
                                Sort.Direction.DESC,
                                "sellingPrice"
                        );

                break;


            case "name":

                sorting =
                        Sort.by(
                                Sort.Direction.ASC,
                                "name"
                        );

                break;


            default:

                sorting =
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        );

                break;
        }


        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sorting
                );


        Page<Product> products =
                productRepository.findAll(
                        ProductSpecification.filter(
                                keyword,
                                categoryId,
                                subCategoryId,
                                brandId,
                                minPrice,
                                maxPrice
                        ),
                        pageable
                );


        model.addAttribute(
                "products",
                products
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "categoryId",
                categoryId
        );

        model.addAttribute(
                "subCategoryId",
                subCategoryId
        );

        model.addAttribute(
                "brandId",
                brandId
        );

        model.addAttribute(
                "minPrice",
                minPrice
        );

        model.addAttribute(
                "maxPrice",
                maxPrice
        );

        model.addAttribute(
                "sort",
                sort
        );


        model.addAttribute(
                "categories",
                categoryRepository
                        .findByEnabledTrueOrderByNameAsc()
        );

        model.addAttribute(
                "subCategories",
                subCategoryRepository
                        .findByOrderByNameAsc()
        );

        model.addAttribute(
                "brands",
                brandRepository
                        .findByEnabledTrueOrderByNameAsc()
        );


        return "customer/products";
    }


    // =========================================================
    // PUBLIC - PRODUCT DETAILS
    // =========================================================

    @GetMapping("/products/{slug}")
    public String productDetails(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int reviewPage,
            Model model) {

        if (reviewPage < 0) {
            reviewPage = 0;
        }


        Product product =
                productRepository.findBySlug(slug)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found."
                                )
                        );


        // =====================================================
        // PRODUCT IMAGES
        // =====================================================

        List<ProductImage> images =
                product.getImages()
                        .stream()
                        .filter(ProductImage::isEnabled)
                        .sorted(
                                (a, b) ->
                                        Integer.compare(
                                                a.getSortOrder(),
                                                b.getSortOrder()
                                        )
                        )
                        .toList();


        // =====================================================
        // REVIEWS
        // =====================================================

        Pageable reviewPageable =
                PageRequest.of(
                        reviewPage,
                        5,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );


        Page<org.example.dreamzshop.entity.Review> reviews =
                reviewService.getProductReviews(
                        product.getId(),
                        reviewPageable
                );


        // =====================================================
        // RATING SUMMARY
        // =====================================================

        Double averageRating =
                reviewService.getAverageRating(
                        product.getId()
                );

        if (averageRating == null) {
            averageRating = 0.0;
        }


        long reviewCount =
                reviewService.getReviewCount(
                        product.getId()
                );


        long fiveStarCount =
                reviewService.getRatingCount(
                        product.getId(),
                        5
                );


        long fourStarCount =
                reviewService.getRatingCount(
                        product.getId(),
                        4
                );


        long threeStarCount =
                reviewService.getRatingCount(
                        product.getId(),
                        3
                );


        long twoStarCount =
                reviewService.getRatingCount(
                        product.getId(),
                        2
                );


        long oneStarCount =
                reviewService.getRatingCount(
                        product.getId(),
                        1
                );


        // =====================================================
        // MODEL
        // =====================================================

        model.addAttribute(
                "product",
                product
        );

        model.addAttribute(
                "images",
                images
        );

        model.addAttribute(
                "reviews",
                reviews
        );

        model.addAttribute(
                "reviewPage",
                reviewPage
        );

        model.addAttribute(
                "averageRating",
                averageRating
        );

        model.addAttribute(
                "reviewCount",
                reviewCount
        );

        model.addAttribute(
                "fiveStarCount",
                fiveStarCount
        );

        model.addAttribute(
                "fourStarCount",
                fourStarCount
        );

        model.addAttribute(
                "threeStarCount",
                threeStarCount
        );

        model.addAttribute(
                "twoStarCount",
                twoStarCount
        );

        model.addAttribute(
                "oneStarCount",
                oneStarCount
        );


        return "customer/product-details";
    }


    // =========================================================
    // LOAD PRODUCT FORM DATA
    // =========================================================

    private void loadProductFormData(
            Model model) {

        model.addAttribute(
                "categories",
                categoryRepository
                        .findAllByOrderByNameAsc()
        );

        model.addAttribute(
                "subCategories",
                subCategoryRepository
                        .findByOrderByNameAsc()
        );

        model.addAttribute(
                "brands",
                brandRepository
                        .findByOrderByNameAsc()
        );

        // IMPORTANT:
        // Required for product status dropdown.
        model.addAttribute(
                "statuses",
                ProductStatus.values()
        );
    }


    // =========================================================
    // VALIDATE PRODUCT PRICING
    // =========================================================

    private boolean validatePricing(
            Product product,
            BindingResult result) {

        boolean valid = true;


        if (product.getMrp() != null
                && product.getSellingPrice() != null
                && product.getSellingPrice()
                .compareTo(
                        product.getMrp()
                ) > 0) {

            result.rejectValue(
                    "sellingPrice",
                    "error.product",
                    "Selling price cannot be greater than MRP."
            );

            valid = false;
        }


        if (product.getTaxPercentage() != null
                && (
                product.getTaxPercentage()
                        .compareTo(
                                BigDecimal.ZERO
                        ) < 0
                        ||
                        product.getTaxPercentage()
                                .compareTo(
                                        new BigDecimal("100")
                                ) > 0
        )) {

            result.rejectValue(
                    "taxPercentage",
                    "error.product",
                    "Tax percentage must be between 0 and 100."
            );

            valid = false;
        }


        return valid;
    }
}
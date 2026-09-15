package org.example.dreamzshop.controller;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.Banner;
import org.example.dreamzshop.entity.Category;
import org.example.dreamzshop.entity.Offer;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.repository.BannerRepository;
import org.example.dreamzshop.repository.CategoryRepository;
import org.example.dreamzshop.repository.OfferRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final BannerRepository bannerRepository;

    private final OfferRepository offerRepository;


    // =========================================================
    // CUSTOMER HOME PAGE
    // =========================================================

    @GetMapping("/")
    public String home(Model model) {

        LocalDateTime now =
                LocalDateTime.now();


        // =====================================================
        // ALL ACTIVE BACKEND BANNERS
        // =====================================================

        List<Banner> banners =
                bannerRepository.findActiveBanners(
                        now
                );

        model.addAttribute(
                "banners",
                banners
        );


        // =====================================================
        // ALL CURRENTLY ACTIVE BACKEND OFFERS
        // =====================================================

        List<Offer> offers =
                offerRepository.findCurrentlyActiveOffers(
                        now
                );

        model.addAttribute(
                "offers",
                offers
        );


        // =====================================================
        // ACTIVE CATEGORIES
        // =====================================================

        List<Category> categories =
                categoryRepository
                        .findByEnabledTrueOrderByNameAsc();

        model.addAttribute(
                "categories",
                categories
        );


        // =====================================================
        // FEATURED PRODUCTS
        // =====================================================

        Page<Product> featuredProducts =
                productRepository
                        .findByFeaturedTrueAndStatus(
                                ProductStatus.ACTIVE,
                                PageRequest.of(
                                        0,
                                        8,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "createdAt"
                                        )
                                )
                        );

        model.addAttribute(
                "featuredProducts",
                featuredProducts.getContent()
        );


        // =====================================================
        // LATEST PRODUCTS
        // =====================================================

        Page<Product> latestProducts =
                productRepository
                        .findByStatus(
                                ProductStatus.ACTIVE,
                                PageRequest.of(
                                        0,
                                        8,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "createdAt"
                                        )
                                )
                        );

        model.addAttribute(
                "latestProducts",
                latestProducts.getContent()
        );


        // =====================================================
        // PRODUCTS WITH DIRECT PRICE DISCOUNT
        // =====================================================

        List<Product> activeProducts =
                productRepository
                        .findByStatus(
                                ProductStatus.ACTIVE,
                                PageRequest.of(
                                        0,
                                        50,
                                        Sort.by(
                                                Sort.Direction.DESC,
                                                "createdAt"
                                        )
                                )
                        )
                        .getContent();


        List<Product> discountProducts =
                activeProducts
                        .stream()
                        .filter(product ->
                                product.getMrp() != null
                                        && product.getSellingPrice() != null
                                        && product.getMrp()
                                        .compareTo(
                                                product.getSellingPrice()
                                        ) > 0
                        )
                        .limit(8)
                        .toList();

        model.addAttribute(
                "discountProducts",
                discountProducts
        );


        // =====================================================
        // PAGE INFORMATION
        // =====================================================

        model.addAttribute(
                "pageTitle",
                "Dreamz Shop"
        );

        model.addAttribute(
                "pageDescription",
                "Shop quality products at great prices."
        );


        return "home";
    }


    // =========================================================
    // LOGIN PAGE
    // =========================================================

    @GetMapping("/login")
    public String login() {

        return "login";
    }
}
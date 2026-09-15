package org.example.dreamzshop.controller;

import org.example.dreamzshop.entity.Wishlist;
import org.example.dreamzshop.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String viewWishlist(
            Authentication authentication,
            Model model
    ) {

        Wishlist wishlist =
                wishlistService.getOrCreateWishlist(
                        authentication.getName()
                );

        model.addAttribute("wishlist", wishlist);

        return "customer/wishlist";
    }

    @PostMapping("/add")
    public String addToWishlist(
            @RequestParam Long productId,
            Authentication authentication
    ) {

        try {

            wishlistService.addToWishlist(
                    authentication.getName(),
                    productId
            );

            return "redirect:/customer/wishlist?added=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/products?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public String removeItem(
            @RequestParam Long wishlistItemId,
            Authentication authentication
    ) {

        try {

            wishlistService.removeItem(
                    authentication.getName(),
                    wishlistItemId
            );

            return "redirect:/customer/wishlist?removed=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/wishlist?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/remove-product")
    public String removeProduct(
            @RequestParam Long productId,
            Authentication authentication
    ) {

        try {

            wishlistService.removeProduct(
                    authentication.getName(),
                    productId
            );

            return "redirect:/customer/wishlist?removed=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/wishlist?error="
                    + encode(e.getMessage());
        }
    }

    private String encode(String message) {

        if (message == null) {
            return "Something went wrong";
        }

        return message
                .replace(" ", "%20")
                .replace("?", "%3F")
                .replace("&", "%26");
    }
}
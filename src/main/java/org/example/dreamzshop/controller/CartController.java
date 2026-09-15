package org.example.dreamzshop.controller;

import org.example.dreamzshop.entity.Cart;
import org.example.dreamzshop.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(
            Authentication authentication,
            Model model
    ) {

        Cart cart = cartService.getOrCreateCart(
                authentication.getName()
        );

        model.addAttribute("cart", cart);

        return "customer/cart";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication
    ) {

        try {

            cartService.addToCart(
                    authentication.getName(),
                    productId,
                    quantity
            );

            return "redirect:/customer/cart?added=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/products?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication
    ) {

        try {

            cartService.updateQuantity(
                    authentication.getName(),
                    cartItemId,
                    quantity
            );

            return "redirect:/customer/cart?updated=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/cart?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public String removeItem(
            @RequestParam Long cartItemId,
            Authentication authentication
    ) {

        try {

            cartService.removeItem(
                    authentication.getName(),
                    cartItemId
            );

            return "redirect:/customer/cart?removed=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/cart?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/clear")
    public String clearCart(
            Authentication authentication
    ) {

        try {

            cartService.clearCart(
                    authentication.getName()
            );

            return "redirect:/customer/cart?cleared=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/cart?error="
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
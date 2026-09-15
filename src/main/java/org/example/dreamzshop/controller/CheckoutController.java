package org.example.dreamzshop.controller;

import jakarta.servlet.http.HttpSession;
import org.example.dreamzshop.dto.PricingResult;
import org.example.dreamzshop.entity.Address;
import org.example.dreamzshop.entity.Cart;
import org.example.dreamzshop.service.AddressService;
import org.example.dreamzshop.service.CartService;
import org.example.dreamzshop.service.PricingService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer/checkout")
public class CheckoutController {

    private static final String COUPON_SESSION_KEY =
            "dreamzshop_applied_coupon";

    private final CartService cartService;

    private final AddressService addressService;

    private final PricingService pricingService;

    private final org.example.dreamzshop.service.OrderService orderService;

    public CheckoutController(
            CartService cartService,
            AddressService addressService,
            PricingService pricingService,
            org.example.dreamzshop.service.OrderService orderService
    ) {

        this.cartService =
                cartService;

        this.addressService =
                addressService;

        this.pricingService =
                pricingService;

        this.orderService =
                orderService;
    }

    @GetMapping
    public String checkout(
            Authentication authentication,
            HttpSession session,
            Model model
    ) {

        try {

            String email =
                    authentication.getName();

            Cart cart =
                    cartService.getOrCreateCart(
                            email
                    );

            if (cart.getItems() == null
                    || cart.getItems().isEmpty()) {

                return "redirect:/customer/cart?error=Your%20cart%20is%20empty";
            }

            List<Address> addresses =
                    addressService.getCustomerAddresses(
                            email
                    );

            if (addresses.isEmpty()) {

                return "redirect:/customer/addresses/add?checkout=true";
            }

            String couponCode =
                    getAppliedCoupon(session);

            PricingResult pricing =
                    pricingService.calculateCartPricing(
                            cart,
                            couponCode
                    );

            model.addAttribute(
                    "cart",
                    cart
            );

            model.addAttribute(
                    "addresses",
                    addresses
            );

            model.addAttribute(
                    "pricing",
                    pricing
            );

            model.addAttribute(
                    "appliedCoupon",
                    couponCode
            );

            return "customer/checkout";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/cart?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(
            @RequestParam String couponCode,
            Authentication authentication,
            HttpSession session
    ) {

        try {

            String email =
                    authentication.getName();

            Cart cart =
                    cartService.getOrCreateCart(
                            email
                    );

            if (cart.getItems() == null
                    || cart.getItems().isEmpty()) {

                return "redirect:/customer/cart?error=Your%20cart%20is%20empty";
            }

            PricingResult pricing =
                    pricingService.calculateCartPricing(
                            cart,
                            couponCode
                    );

            if (pricing.getCouponDiscount()
                    .compareTo(
                            java.math.BigDecimal.ZERO
                    ) <= 0) {

                return "redirect:/customer/checkout?error=Coupon%20could%20not%20be%20applied";
            }

            session.setAttribute(
                    COUPON_SESSION_KEY,
                    pricing.getCouponCode()
            );

            return "redirect:/customer/checkout?couponApplied=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/checkout?error="
                    + encode(e.getMessage());
        }
    }

    @PostMapping("/remove-coupon")
    public String removeCoupon(
            HttpSession session
    ) {

        session.removeAttribute(
                COUPON_SESSION_KEY
        );

        return "redirect:/customer/checkout?couponRemoved=true";
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam Long addressId,
            Authentication authentication,
            HttpSession session
    ) {

        try {

            String couponCode =
                    getAppliedCoupon(session);

            var order =
                    orderService.createCodOrder(
                            authentication.getName(),
                            addressId,
                            couponCode
                    );

            /*
             * Coupon should no longer remain in session
             * after successful order.
             */
            session.removeAttribute(
                    COUPON_SESSION_KEY
            );

            return "redirect:/customer/orders/"
                    + order.getId()
                    + "?created=true";

        } catch (IllegalArgumentException e) {

            return "redirect:/customer/checkout?error="
                    + encode(e.getMessage());
        }
    }

    private String getAppliedCoupon(
            HttpSession session
    ) {

        Object value =
                session.getAttribute(
                        COUPON_SESSION_KEY
                );

        if (value == null) {
            return null;
        }

        return value.toString();
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
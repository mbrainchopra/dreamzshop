package org.example.dreamzshop.service;

import org.example.dreamzshop.dto.PricingResult;
import org.example.dreamzshop.entity.Cart;

public interface PricingService {

    PricingResult calculateCartPricing(
            Cart cart,
            String couponCode
    );
}
package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Cart;

public interface CartService {

    Cart getOrCreateCart(String email);

    void addToCart(String email, Long productId, Integer quantity);

    void updateQuantity(String email, Long cartItemId, Integer quantity);

    void removeItem(String email, Long cartItemId);

    void clearCart(String email);
}
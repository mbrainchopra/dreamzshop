package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Wishlist;

public interface WishlistService {

    Wishlist getOrCreateWishlist(String email);

    void addToWishlist(String email, Long productId);

    void removeItem(String email, Long wishlistItemId);

    void removeProduct(String email, Long productId);

    boolean isProductInWishlist(String email, Long productId);
}
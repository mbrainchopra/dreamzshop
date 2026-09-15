package org.example.dreamzshop.service.impl;

import org.example.dreamzshop.entity.Cart;
import org.example.dreamzshop.entity.CartItem;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.repository.CartItemRepository;
import org.example.dreamzshop.repository.CartRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Cart getOrCreateCart(String email) {

        User user = getCustomer(email);

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {

                    Cart cart = Cart.builder()
                            .user(user)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return cartRepository.save(cart);
                });
    }

    @Override
    public void addToCart(
            String email,
            Long productId,
            Integer quantity
    ) {

        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1"
            );
        }

        User user = getCustomer(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"
                        )
                );

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "This product is currently unavailable"
            );
        }

        if (product.getStockQuantity() == null
                || product.getStockQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "This product is out of stock"
            );
        }

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {

                    Cart newCart = Cart.builder()
                            .user(user)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return cartRepository.save(newCart);
                });

        CartItem existingItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                product.getId()
                        )
                        .orElse(null);

        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity() + quantity;

            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException(
                        "Only "
                                + product.getStockQuantity()
                                + " item(s) are available in stock"
                );
            }

            existingItem.setQuantity(newQuantity);

            /*
             * Refresh the price with the current selling price.
             * This ensures the cart does not keep an outdated
             * price when the product price changes.
             */
            existingItem.setUnitPrice(
                    product.getSellingPrice()
            );

            existingItem.setUpdatedAt(
                    LocalDateTime.now()
            );

            cartItemRepository.save(existingItem);

        } else {

            if (quantity > product.getStockQuantity()) {
                throw new IllegalArgumentException(
                        "Only "
                                + product.getStockQuantity()
                                + " item(s) are available in stock"
                );
            }

            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getSellingPrice())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            cart.addItem(cartItem);

            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }

    @Override
    public void updateQuantity(
            String email,
            Long cartItemId,
            Integer quantity
    ) {

        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1"
            );
        }

        User user = getCustomer(email);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found"
                        )
                );

        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found"
                        )
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException(
                    "You are not authorized to modify this cart item"
            );
        }

        Product product = cartItem.getProduct();

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "This product is currently unavailable"
            );
        }

        if (product.getStockQuantity() == null
                || product.getStockQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "This product is out of stock"
            );
        }

        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException(
                    "Only "
                            + product.getStockQuantity()
                            + " item(s) are available in stock"
            );
        }

        cartItem.setQuantity(quantity);

        /*
         * Always use the latest selling price when
         * the cart quantity is updated.
         */
        cartItem.setUnitPrice(
                product.getSellingPrice()
        );

        cartItem.setUpdatedAt(LocalDateTime.now());

        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }

    @Override
    public void removeItem(
            String email,
            Long cartItemId
    ) {

        User user = getCustomer(email);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found"
                        )
                );

        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found"
                        )
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException(
                    "You are not authorized to remove this cart item"
            );
        }

        cart.removeItem(cartItem);

        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }

    @Override
    public void clearCart(String email) {

        User user = getCustomer(email);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart not found"
                        )
                );

        cart.getItems().clear();

        cartItemRepository.deleteByCartId(cart.getId());

        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
    }

    private User getCustomer(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User account not found"
                        )
                );

        if (user.getRole() == null
                || !user.getRole().name().equals("CUSTOMER")) {

            throw new IllegalArgumentException(
                    "Only customers can use the shopping cart"
            );
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "Your account is disabled"
            );
        }

        return user;
    }
}
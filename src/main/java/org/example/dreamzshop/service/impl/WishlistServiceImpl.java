package org.example.dreamzshop.service.impl;

import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.entity.Wishlist;
import org.example.dreamzshop.entity.WishlistItem;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.repository.WishlistItemRepository;
import org.example.dreamzshop.repository.WishlistRepository;
import org.example.dreamzshop.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    private final WishlistItemRepository wishlistItemRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;


    public WishlistServiceImpl(
            WishlistRepository wishlistRepository,
            WishlistItemRepository wishlistItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {

        this.wishlistRepository = wishlistRepository;

        this.wishlistItemRepository =
                wishlistItemRepository;

        this.productRepository =
                productRepository;

        this.userRepository =
                userRepository;
    }


    // =========================================================
    // GET OR CREATE WISHLIST
    // =========================================================

    @Override
    public Wishlist getOrCreateWishlist(
            String email
    ) {

        User user = getCustomer(email);

        return wishlistRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {

                    Wishlist wishlist =
                            Wishlist.builder()
                                    .user(user)
                                    .createdAt(
                                            LocalDateTime.now()
                                    )
                                    .updatedAt(
                                            LocalDateTime.now()
                                    )
                                    .build();

                    return wishlistRepository.save(
                            wishlist
                    );
                });
    }


    // =========================================================
    // ADD PRODUCT TO WISHLIST
    // =========================================================

    @Override
    public void addToWishlist(
            String email,
            Long productId
    ) {

        User user = getCustomer(email);

        if (productId == null) {

            throw new IllegalArgumentException(
                    "Product ID is required"
            );
        }

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found"
                                )
                        );


        if (product.getStatus()
                != ProductStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "This product is currently unavailable"
            );
        }


        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(user.getId())
                        .orElseGet(() -> {

                            Wishlist newWishlist =
                                    Wishlist.builder()
                                            .user(user)
                                            .createdAt(
                                                    LocalDateTime.now()
                                            )
                                            .updatedAt(
                                                    LocalDateTime.now()
                                            )
                                            .build();

                            return wishlistRepository.save(
                                    newWishlist
                            );
                        });


        boolean alreadyExists =
                wishlistItemRepository
                        .findByWishlistIdAndProductId(
                                wishlist.getId(),
                                product.getId()
                        )
                        .isPresent();


        if (alreadyExists) {

            throw new IllegalArgumentException(
                    "Product is already in your wishlist"
            );
        }


        WishlistItem item =
                WishlistItem.builder()
                        .wishlist(wishlist)
                        .product(product)
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();


        wishlist.addItem(item);

        wishlistItemRepository.save(item);

        wishlist.setUpdatedAt(
                LocalDateTime.now()
        );

        wishlistRepository.save(wishlist);
    }


    // =========================================================
    // REMOVE WISHLIST ITEM
    // =========================================================

    @Override
    public void removeItem(
            String email,
            Long wishlistItemId
    ) {

        User user = getCustomer(email);

        if (wishlistItemId == null) {

            throw new IllegalArgumentException(
                    "Wishlist item ID is required"
            );
        }


        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Wishlist not found"
                                )
                        );


        WishlistItem item =
                wishlistItemRepository
                        .findById(wishlistItemId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Wishlist item not found"
                                )
                        );


        if (item.getWishlist() == null
                || !item.getWishlist()
                .getId()
                .equals(wishlist.getId())) {

            throw new IllegalArgumentException(
                    "You are not authorized to modify this wishlist item"
            );
        }


        wishlist.removeItem(item);

        wishlistItemRepository.delete(item);

        wishlist.setUpdatedAt(
                LocalDateTime.now()
        );

        wishlistRepository.save(wishlist);
    }


    // =========================================================
    // REMOVE PRODUCT FROM WISHLIST
    // =========================================================

    @Override
    public void removeProduct(
            String email,
            Long productId
    ) {

        User user = getCustomer(email);

        if (productId == null) {

            throw new IllegalArgumentException(
                    "Product ID is required"
            );
        }


        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Wishlist not found"
                                )
                        );


        WishlistItem item =
                wishlistItemRepository
                        .findByWishlistIdAndProductId(
                                wishlist.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product is not in your wishlist"
                                )
                        );


        wishlist.removeItem(item);

        wishlistItemRepository.delete(item);

        wishlist.setUpdatedAt(
                LocalDateTime.now()
        );

        wishlistRepository.save(wishlist);
    }


    // =========================================================
    // CHECK PRODUCT IN WISHLIST
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public boolean isProductInWishlist(
            String email,
            Long productId
    ) {

        User user = getCustomer(email);

        if (productId == null) {
            return false;
        }


        Wishlist wishlist =
                wishlistRepository
                        .findByUserId(user.getId())
                        .orElse(null);


        if (wishlist == null) {
            return false;
        }


        return wishlistItemRepository
                .findByWishlistIdAndProductId(
                        wishlist.getId(),
                        productId
                )
                .isPresent();
    }


    // =========================================================
    // GET CUSTOMER
    // =========================================================

    private User getCustomer(
            String email
    ) {

        if (email == null
                || email.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "User email is required"
            );
        }


        User user =
                userRepository
                        .findByEmail(
                                email.trim()
                                        .toLowerCase()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"
                                )
                        );


        if (user.getRole() == null
                || !user.getRole()
                .name()
                .equals("CUSTOMER")) {

            throw new IllegalArgumentException(
                    "Only customers can use the wishlist"
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
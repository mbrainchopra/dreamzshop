package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByWishlistIdAndProductId(
            Long wishlistId,
            Long productId
    );

    void deleteByWishlistId(Long wishlistId);

    long countByProductId(Long productId);
}
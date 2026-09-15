package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository
        extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
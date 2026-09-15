package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdAndEnabledTrueOrderBySortOrderAsc(
            Long productId
    );

    Optional<ProductImage> findByProductIdAndPrimaryImageTrue(
            Long productId
    );

    long countByProductIdAndEnabledTrue(
            Long productId
    );

    void deleteByProductId(Long productId);
}
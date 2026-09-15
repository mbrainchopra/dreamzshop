package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    Optional<Product> findBySlug(String slug);


    // =========================================================
    // EXISTENCE CHECKS
    // =========================================================

    boolean existsBySku(String sku);

    boolean existsByBarcode(String barcode);

    boolean existsBySlug(String slug);


    // =========================================================
    // STATUS
    // =========================================================

    Page<Product> findByStatus(
            ProductStatus status,
            Pageable pageable
    );


    // =========================================================
    // FEATURED PRODUCTS
    // =========================================================

    Page<Product> findByFeaturedTrueAndStatus(
            ProductStatus status,
            Pageable pageable
    );


    // =========================================================
    // SEARCH
    // =========================================================

    Page<Product> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<Product> findBySkuContainingIgnoreCase(
            String sku,
            Pageable pageable
    );

    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String name,
            String sku,
            Pageable pageable
    );


    // =========================================================
    // SEARCH + STATUS
    // =========================================================

    Page<Product> findByStatusAndNameContainingIgnoreCase(
            ProductStatus status,
            String name,
            Pageable pageable
    );


    // =========================================================
    // COUNTS
    // =========================================================

    long countByStatus(ProductStatus status);

    long countByFeaturedTrue();

    long countByStockQuantity(Integer stockQuantity);


    // =========================================================
    // LOW STOCK
    // =========================================================

    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.stockQuantity > 0
              AND p.minimumStockLevel IS NOT NULL
              AND p.stockQuantity <= p.minimumStockLevel
            """)
    long countLowStockProducts();


    // =========================================================
    // OUT OF STOCK
    // =========================================================

    @Query("""
            SELECT COUNT(p)
            FROM Product p
            WHERE p.stockQuantity = 0
            """)
    long countOutOfStockProducts();


    // =========================================================
    // LOW STOCK PRODUCT LIST
    // =========================================================

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.stockQuantity > 0
              AND p.minimumStockLevel IS NOT NULL
              AND p.stockQuantity <= p.minimumStockLevel
            """)
    Page<Product> findLowStockProducts(
            Pageable pageable
    );


    // =========================================================
    // OUT OF STOCK PRODUCT LIST
    // =========================================================

    Page<Product> findByStockQuantity(
            Integer stockQuantity,
            Pageable pageable
    );


    // =========================================================
    // STOCK QUANTITY
    // =========================================================

    @Query("""
            SELECT p.stockQuantity
            FROM Product p
            WHERE p.id = :productId
            """)
    Integer findStockQuantityByProductId(
            @Param("productId") Long productId
    );
}
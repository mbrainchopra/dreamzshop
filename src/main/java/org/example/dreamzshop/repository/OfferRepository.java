package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OfferRepository
        extends JpaRepository<Offer, Long> {


    Page<Offer> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );


    List<Offer> findByEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDateTime startDate,
            LocalDateTime endDate
    );


    @Query("""
            SELECT o
            FROM Offer o
            WHERE o.enabled = true
            AND o.startDate <= :now
            AND o.endDate >= :now
            ORDER BY o.discountValue DESC
            """)
    List<Offer> findCurrentlyActiveOffers(
            @Param("now") LocalDateTime now
    );


    @Query("""
            SELECT o
            FROM Offer o
            WHERE o.enabled = true
            AND o.startDate <= :now
            AND o.endDate >= :now
            AND (
                o.product.id = :productId
                OR o.category.id = :categoryId
                OR o.brand.id = :brandId
                OR (
                    o.product IS NULL
                    AND o.category IS NULL
                    AND o.brand IS NULL
                )
            )
            ORDER BY
                CASE
                    WHEN o.product.id = :productId THEN 1
                    WHEN o.category.id = :categoryId THEN 2
                    WHEN o.brand.id = :brandId THEN 3
                    ELSE 4
                END ASC,
                o.discountValue DESC
            """)
    List<Offer> findEligibleOffers(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("now") LocalDateTime now
    );


    List<Offer> findByProductId(
            Long productId
    );


    List<Offer> findByCategoryId(
            Long categoryId
    );


    List<Offer> findByBrandId(
            Long brandId
    );


    long countByEnabledTrue();


    long countByEndDateBefore(
            LocalDateTime dateTime
    );

}
package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface BannerRepository
        extends JpaRepository<Banner, Long> {


    // =========================================================
    // ADMIN
    // =========================================================

    Page<Banner> findAllByOrderByDisplayOrderAsc(
            Pageable pageable
    );


    List<Banner> findAllByOrderByDisplayOrderAsc();


    Page<Banner> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );


    long countByEnabledTrue();


    long countByEnabledFalse();


    // =========================================================
    // CUSTOMER / HOMEPAGE
    // =========================================================

    @Query("""
            SELECT b
            FROM Banner b
            WHERE b.enabled = true
              AND (b.startDate IS NULL OR b.startDate <= :now)
              AND (b.endDate IS NULL OR b.endDate >= :now)
            ORDER BY b.displayOrder ASC
            """)
    List<Banner> findActiveBanners(
            LocalDateTime now
    );
}
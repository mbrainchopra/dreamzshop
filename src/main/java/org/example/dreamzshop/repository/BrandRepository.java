package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository
        extends JpaRepository<Brand, Long> {

    Optional<Brand> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Brand> findByEnabledTrueOrderByNameAsc();

    List<Brand> findByOrderByNameAsc();
}
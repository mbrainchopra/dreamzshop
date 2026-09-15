package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByEnabledTrueOrderByNameAsc();

    List<Category> findAllByOrderByNameAsc();
}
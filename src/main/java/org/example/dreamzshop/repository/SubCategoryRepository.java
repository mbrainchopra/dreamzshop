package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryRepository
        extends JpaRepository<SubCategory, Long> {

    List<SubCategory> findByCategoryIdAndEnabledTrueOrderByNameAsc(
            Long categoryId
    );

    List<SubCategory> findByOrderByNameAsc();

    boolean existsByCategoryIdAndNameIgnoreCase(
            Long categoryId,
            String name
    );
}
package org.example.dreamzshop.util;

import jakarta.persistence.criteria.Predicate;
import org.example.dreamzshop.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> filter(
            String keyword,
            Long categoryId,
            Long subCategoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();


            // -------------------------------------------------
            // ONLY ACTIVE PRODUCTS FOR CUSTOMER SIDE
            // -------------------------------------------------

            predicates.add(
                    criteriaBuilder.equal(
                            root.get("status"),
                            org.example.dreamzshop.enums.ProductStatus.ACTIVE
                    )
            );


            // -------------------------------------------------
            // KEYWORD SEARCH
            // -------------------------------------------------

            if (keyword != null
                    && !keyword.trim().isEmpty()) {

                String search =
                        "%" + keyword.trim().toLowerCase() + "%";

                Predicate namePredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("name")
                                ),
                                search
                        );

                Predicate skuPredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("sku")
                                ),
                                search
                        );

                Predicate descriptionPredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("description")
                                ),
                                search
                        );

                predicates.add(
                        criteriaBuilder.or(
                                namePredicate,
                                skuPredicate,
                                descriptionPredicate
                        )
                );
            }


            // -------------------------------------------------
            // CATEGORY
            // -------------------------------------------------

            if (categoryId != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("category").get("id"),
                                categoryId
                        )
                );
            }


            // -------------------------------------------------
            // SUB CATEGORY
            // -------------------------------------------------

            if (subCategoryId != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("subCategory").get("id"),
                                subCategoryId
                        )
                );
            }


            // -------------------------------------------------
            // BRAND
            // -------------------------------------------------

            if (brandId != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("brand").get("id"),
                                brandId
                        )
                );
            }


            // -------------------------------------------------
            // MINIMUM PRICE
            // -------------------------------------------------

            if (minPrice != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("sellingPrice"),
                                minPrice
                        )
                );
            }


            // -------------------------------------------------
            // MAXIMUM PRICE
            // -------------------------------------------------

            if (maxPrice != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("sellingPrice"),
                                maxPrice
                        )
                );
            }


            return criteriaBuilder.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };
    }
}
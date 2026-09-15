package org.example.dreamzshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.entity.InventoryTransaction;
import org.example.dreamzshop.entity.Product;
import org.example.dreamzshop.enums.InventoryTransactionType;
import org.example.dreamzshop.enums.ProductStatus;
import org.example.dreamzshop.repository.InventoryTransactionRepository;
import org.example.dreamzshop.repository.ProductRepository;
import org.example.dreamzshop.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    private final InventoryTransactionRepository
            inventoryTransactionRepository;


    // =========================================================
    // GET ALL PRODUCTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(
            String keyword,
            Pageable pageable
    ) {

        if (keyword == null
                || keyword.trim().isEmpty()) {

            return productRepository.findAll(pageable);
        }

        return productRepository
                .findByNameContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                );
    }


    // =========================================================
    // GET PRODUCT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(
            Long productId
    ) {

        if (productId == null) {

            throw new IllegalArgumentException(
                    "Product ID is required"
            );
        }

        return productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"
                        )
                );
    }


    // =========================================================
    // STOCK IN
    // =========================================================

    @Override
    public InventoryTransaction addStock(
            Long productId,
            Integer quantity,
            String reason
    ) {

        validateQuantity(quantity);

        Product product =
                getProduct(productId);

        int previousStock =
                safeStock(
                        product.getStockQuantity()
                );

        long calculatedStock =
                (long) previousStock + quantity;

        if (calculatedStock > Integer.MAX_VALUE) {

            throw new IllegalArgumentException(
                    "Stock quantity is too large"
            );
        }

        int newStock =
                (int) calculatedStock;

        product.setStockQuantity(
                newStock
        );

        updateProductStatus(product);

        productRepository.save(product);


        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .product(product)
                        .quantity(quantity)
                        .previousStock(previousStock)
                        .newStock(newStock)
                        .type(
                                InventoryTransactionType.STOCK_IN
                        )
                        .reason(
                                normalizeReason(reason)
                        )
                        .build();


        return inventoryTransactionRepository
                .save(transaction);
    }


    // =========================================================
    // STOCK OUT
    // =========================================================

    @Override
    public InventoryTransaction removeStock(
            Long productId,
            Integer quantity,
            String reason
    ) {

        validateQuantity(quantity);

        Product product =
                getProduct(productId);

        int previousStock =
                safeStock(
                        product.getStockQuantity()
                );


        if (quantity > previousStock) {

            throw new IllegalArgumentException(
                    "Stock out quantity cannot exceed current stock"
            );
        }


        int newStock =
                previousStock - quantity;

        product.setStockQuantity(
                newStock
        );

        updateProductStatus(product);

        productRepository.save(product);


        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .product(product)
                        .quantity(quantity)
                        .previousStock(previousStock)
                        .newStock(newStock)
                        .type(
                                InventoryTransactionType.STOCK_OUT
                        )
                        .reason(
                                normalizeReason(reason)
                        )
                        .build();


        return inventoryTransactionRepository
                .save(transaction);
    }


    // =========================================================
    // ADJUST STOCK
    // =========================================================

    @Override
    public InventoryTransaction adjustStock(
            Long productId,
            Integer newStock,
            String reason
    ) {

        if (newStock == null
                || newStock < 0) {

            throw new IllegalArgumentException(
                    "Stock cannot be negative"
            );
        }


        Product product =
                getProduct(productId);

        int previousStock =
                safeStock(
                        product.getStockQuantity()
                );


        int difference =
                Math.abs(
                        newStock - previousStock
                );


        product.setStockQuantity(
                newStock
        );

        updateProductStatus(product);

        productRepository.save(product);


        InventoryTransaction transaction =
                InventoryTransaction.builder()
                        .product(product)
                        .quantity(difference)
                        .previousStock(previousStock)
                        .newStock(newStock)
                        .type(
                                InventoryTransactionType.ADJUSTMENT
                        )
                        .reason(
                                normalizeReason(reason)
                        )
                        .build();


        return inventoryTransactionRepository
                .save(transaction);
    }


    // =========================================================
    // GET INVENTORY TRANSACTIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getTransactions(
            Long productId,
            Pageable pageable
    ) {

        if (productId == null) {

            throw new IllegalArgumentException(
                    "Product ID is required"
            );
        }


        getProduct(productId);


        return inventoryTransactionRepository
                .findByProductId(
                        productId,
                        pageable
                );
    }


    // =========================================================
    // LOW STOCK CHECK
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public boolean isLowStock(
            Product product
    ) {

        if (product == null) {
            return false;
        }


        int stock =
                safeStock(
                        product.getStockQuantity()
                );


        int minimumStockLevel =
                product.getMinimumStockLevel() == null
                        ? 0
                        : Math.max(
                        product.getMinimumStockLevel(),
                        0
                );


        return stock > 0
                && stock <= minimumStockLevel;
    }


    // =========================================================
    // TOTAL PRODUCTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public long getTotalProductCount() {

        return productRepository.count();
    }


    // =========================================================
    // LOW STOCK PRODUCTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public long getLowStockProductCount() {

        return productRepository
                .countLowStockProducts();
    }


    // =========================================================
    // OUT OF STOCK PRODUCTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public long getOutOfStockProductCount() {

        return productRepository
                .countOutOfStockProducts();
    }


    // =========================================================
    // VALIDATE QUANTITY
    // =========================================================

    private void validateQuantity(
            Integer quantity
    ) {

        if (quantity == null
                || quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }


    // =========================================================
    // SAFE STOCK
    // =========================================================

    private int safeStock(
            Integer stockQuantity
    ) {

        if (stockQuantity == null) {
            return 0;
        }

        return Math.max(
                stockQuantity,
                0
        );
    }


    // =========================================================
    // NORMALIZE REASON
    // =========================================================

    private String normalizeReason(
            String reason
    ) {

        if (reason == null
                || reason.trim().isEmpty()) {

            return "Manual inventory update";
        }


        String trimmed =
                reason.trim();


        if (trimmed.length() > 500) {

            return trimmed.substring(
                    0,
                    500
            );
        }


        return trimmed;
    }


    // =========================================================
    // UPDATE PRODUCT STATUS
    // =========================================================

    private void updateProductStatus(
            Product product
    ) {

        int stock =
                safeStock(
                        product.getStockQuantity()
                );


        /*
         * Do not change DISCONTINUED products
         * automatically.
         */

        if (product.getStatus()
                == ProductStatus.DISCONTINUED) {

            return;
        }


        if (stock == 0) {

            product.setStatus(
                    ProductStatus.OUT_OF_STOCK
            );

        } else if (
                product.getStatus()
                        == ProductStatus.OUT_OF_STOCK
        ) {

            product.setStatus(
                    ProductStatus.ACTIVE
            );
        }
    }
}
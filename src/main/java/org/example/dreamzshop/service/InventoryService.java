package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.InventoryTransaction;
import org.example.dreamzshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    Page<Product> getAllProducts(
            String keyword,
            Pageable pageable
    );

    Product getProduct(Long productId);

    InventoryTransaction addStock(
            Long productId,
            Integer quantity,
            String reason
    );

    InventoryTransaction removeStock(
            Long productId,
            Integer quantity,
            String reason
    );

    InventoryTransaction adjustStock(
            Long productId,
            Integer newStock,
            String reason
    );

    Page<InventoryTransaction> getTransactions(
            Long productId,
            Pageable pageable
    );

    boolean isLowStock(Product product);

    long getTotalProductCount();

    long getLowStockProductCount();

    long getOutOfStockProductCount();
}
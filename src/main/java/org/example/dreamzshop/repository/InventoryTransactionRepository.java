package org.example.dreamzshop.repository;

import org.example.dreamzshop.entity.InventoryTransaction;
import org.example.dreamzshop.enums.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {

    Page<InventoryTransaction> findByProductId(
            Long productId,
            Pageable pageable
    );

    Page<InventoryTransaction> findByType(
            InventoryTransactionType type,
            Pageable pageable
    );

    List<InventoryTransaction> findTop20ByProductIdOrderByCreatedAtDesc(
            Long productId
    );
}
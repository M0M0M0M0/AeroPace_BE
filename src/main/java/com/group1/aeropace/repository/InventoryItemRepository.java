package com.group1.aeropace.repository;

import com.group1.aeropace.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductVariantId(Long productVariantId);
}

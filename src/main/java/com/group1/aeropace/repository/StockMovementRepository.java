package com.group1.aeropace.repository;

import com.group1.aeropace.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm WHERE sm.inventoryItem.id = :inventoryItemId")
    Integer sumQuantityByInventoryItemId(@Param("inventoryItemId") Long inventoryItemId);
}

package com.group1.aeropace.service;

import com.group1.aeropace.entity.InventoryItem;
import com.group1.aeropace.entity.ProductVariant;
import com.group1.aeropace.entity.StockMovement;
import com.group1.aeropace.enums.MovementType;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.repository.InventoryItemRepository;
import com.group1.aeropace.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public InventoryItem createInventoryItem(ProductVariant variant, int initialStock) {
        InventoryItem item = new InventoryItem();
        item.setProductVariant(variant);
        item.setSku(variant.getSku());
        item.setCachedStock(0);
        InventoryItem saved = inventoryItemRepository.save(item);

        if (initialStock > 0) {
            recordMovement(saved, MovementType.RESTOCK, initialStock, null, "Initial stock");
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public int getAvailableStock(Long inventoryItemId) {
        InventoryItem item = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        return item.getCachedStock();
    }

    @Transactional(readOnly = true)
    public int getAvailableStockByVariantId(Long variantId) {
        InventoryItem item = inventoryItemRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        return item.getCachedStock();
    }

    @Transactional
    public void deductStock(Long variantId, int quantity, Long orderId) {
        InventoryItem item = inventoryItemRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        if (item.getCachedStock() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        recordMovement(item, MovementType.SALE, -quantity, orderId, null);
    }

    @Transactional
    public void restoreStock(Long variantId, int quantity, Long orderId) {
        InventoryItem item = inventoryItemRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        recordMovement(item, MovementType.CANCEL, quantity, orderId, null);
    }

    @Transactional
    public void restock(Long variantId, int quantity, String note) {
        InventoryItem item = inventoryItemRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        recordMovement(item, MovementType.RESTOCK, quantity, null, note);
    }

    @Transactional
    public void adjust(Long variantId, int delta, String note) {
        InventoryItem item = inventoryItemRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        recordMovement(item, MovementType.ADJUSTMENT, delta, null, note);
    }

    private void recordMovement(InventoryItem item, MovementType type, int quantity, Long orderId, String note) {
        StockMovement movement = new StockMovement();
        movement.setInventoryItem(item);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setReferenceOrderId(orderId);
        movement.setNote(note);
        stockMovementRepository.save(movement);

        item.setCachedStock(item.getCachedStock() + quantity);
        inventoryItemRepository.save(item);
    }
}

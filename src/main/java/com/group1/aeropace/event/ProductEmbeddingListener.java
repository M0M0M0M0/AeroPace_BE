package com.group1.aeropace.event;

import com.group1.aeropace.service.ProductEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEmbeddingListener {

    private final ProductEmbeddingService productEmbeddingService;

    @Async("embeddingTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductChanged(ProductChangedEvent event) {
        Long productId = event.getProductId();
        log.info("[ProductEmbeddingListener] Re-embedding product id={}", productId);
        try {
            productEmbeddingService.embedAndSaveProduct(productId);
        } catch (Exception e) {
            log.error("[ProductEmbeddingListener] Failed to embed product id={}: {}", productId, e.getMessage());
        }
    }
}

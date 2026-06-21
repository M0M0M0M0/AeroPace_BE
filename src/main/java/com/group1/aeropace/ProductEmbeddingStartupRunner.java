package com.group1.aeropace;

import com.group1.aeropace.service.ProductEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEmbeddingStartupRunner {

    private final ProductEmbeddingService productEmbeddingService;

    @Async("embeddingTaskExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        productEmbeddingService.embedMissingActiveProducts();
        productEmbeddingService.warmCache();
    }
}

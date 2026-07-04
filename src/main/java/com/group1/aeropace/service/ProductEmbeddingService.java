package com.group1.aeropace.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group1.aeropace.entity.Product;
import com.group1.aeropace.entity.ProductEmbedding;
import com.group1.aeropace.repository.ProductEmbeddingRepository;
import com.group1.aeropace.repository.ProductRepository;
import com.group1.aeropace.shared.HtmlTextUtils;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {

    private final ProductRepository productRepository;
    private final ProductEmbeddingRepository productEmbeddingRepository;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<Long, float[]> embeddingCache = new ConcurrentHashMap<>();
    private final AtomicBoolean cacheLoaded = new AtomicBoolean(false);

    public void warmCache() {
        embeddingCache.clear();
        cacheLoaded.set(false);
        ensureCacheLoaded();
    }

    private void ensureCacheLoaded() {
        if (cacheLoaded.compareAndSet(false, true)) {
            productEmbeddingRepository.findAll().forEach(pe -> {
                float[] vec = toFloatArray(parseEmbedding(pe.getEmbedding()));
                if (vec.length > 0) embeddingCache.put(pe.getProductId(), vec);
            });
            log.info("[EmbeddingCache] Loaded {} vectors into memory", embeddingCache.size());
        }
    }

    public void embedMissingActiveProducts() {
        List<Long> ids = productRepository.findActiveProductIdsWithoutEmbedding();
        if (ids.isEmpty()) {
            log.info("[EmbeddingStartup] All ACTIVE products already have embeddings");
            return;
        }
        log.info("[EmbeddingStartup] Found {} ACTIVE product(s) without embedding, generating...", ids.size());
        int success = 0;
        for (Long id : ids) {
            try {
                embedAndSaveProduct(id);
                success++;
            } catch (Exception e) {
                log.error("[EmbeddingStartup] Failed product id={}: {}", id, e.getMessage());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("[EmbeddingStartup] Done. {}/{} succeeded.", success, ids.size());
    }

    @Transactional
    public void embedAndSaveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != Product.Status.ACTIVE) {
            log.debug("[ProductEmbeddingService] Skipping product id={}, status={}", productId, product.getStatus());
            return;
        }

        String cleanDescription = HtmlTextUtils.htmlToPlainTextWithPunctuation(product.getDescription());
        String sourceText = product.getName() + ". " + cleanDescription;

        List<Float> vector = embeddingService.embed(sourceText);

        try {
            String embeddingJson = objectMapper.writeValueAsString(vector);
            ProductEmbedding pe = productEmbeddingRepository.findByProductId(productId)
                    .orElseGet(() -> {
                        ProductEmbedding newPe = new ProductEmbedding();
                        newPe.setProductId(productId);
                        return newPe;
                    });
            pe.setSourceText(sourceText);
            pe.setEmbedding(embeddingJson);
            productEmbeddingRepository.save(pe);

            embeddingCache.put(productId, toFloatArray(vector));
        } catch (JsonProcessingException e) {
            log.error("[ProductEmbeddingService] Failed to serialize embedding for product id={}", productId, e);
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    public List<Long> findSimilarProductIds(Long productId, int limit) {
        ensureCacheLoaded();

        float[] sourceVec = embeddingCache.get(productId);
        if (sourceVec == null) return List.of();

        return embeddingCache.entrySet().stream()
                .filter(e -> !e.getKey().equals(productId))
                .map(e -> Map.entry(e.getKey(), cosineSimilarity(sourceVec, e.getValue())))
                .sorted(Map.Entry.<Long, Float>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<Float> parseEmbedding(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Float>>() {});
        } catch (Exception e) {
            log.error("[ProductEmbeddingService] Failed to parse embedding: {}", e.getMessage());
            return List.of();
        }
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length || a.length == 0) return 0f;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0f : (float) (dot / denom);
    }
}

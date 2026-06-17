package com.group1.aeropace.service;

import com.group1.aeropace.entity.*;
import com.group1.aeropace.repository.HistoricalProductRepository;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricalProductService {

    private final HistoricalProductRepository historicalProductRepository;

    @Transactional
    public void createSnapshot(Product product, List<ProductImage> images, List<ProductVariant> variants) {
        LocalDateTime now = LocalDateTime.now();

        // Đóng snapshot đang hiệu lực
        historicalProductRepository.closeCurrentSnapshot(product.getId(), now);

        // Tạo snapshot mới
        HistoricalProduct snapshot = new HistoricalProduct();
        snapshot.setProductId(product.getId());
        snapshot.setName(product.getName());
        snapshot.setDescription(product.getDescription());
        snapshot.setBrand(product.getBrand() != null ? product.getBrand().getName() : null);
        snapshot.setOption1Name(product.getOption1Name());
        snapshot.setOption2Name(product.getOption2Name());
        snapshot.setOption3Name(product.getOption3Name());
        snapshot.setValidFrom(now);
        snapshot.setValidTo(null);

        HistoricalProduct saved = historicalProductRepository.save(snapshot);

        // Sao chép ảnh vào snapshot
        if (images != null) {
            List<HistoricalProductImage> histImages = images.stream()
                    .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                    .map(img -> {
                        HistoricalProductImage hi = new HistoricalProductImage();
                        hi.setHistoricalProduct(saved);
                        hi.setImageUrl(img.getImageUrl());
                        hi.setPosition(img.getPosition());
                        return hi;
                    }).collect(Collectors.toList());
            saved.setImages(histImages);
        }

        // Clone variants (chỉ variant chưa bị xóa)
        if (variants != null) {
            List<HistoricalProductVariant> histVariants = variants.stream()
                    .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()))
                    .map(v -> {
                        HistoricalProductVariant hv = new HistoricalProductVariant();
                        hv.setHistoricalProduct(saved);
                        hv.setOriginalVariantId(v.getId());
                        hv.setSku(v.getSku());
                        hv.setPrice(v.getPrice());
                        hv.setComparePrice(v.getComparePrice());
                        hv.setOption1Value(v.getOption1Value());
                        hv.setOption2Value(v.getOption2Value());
                        hv.setOption3Value(v.getOption3Value());
                        return hv;
                    }).collect(Collectors.toList());
            saved.setVariants(histVariants);
        }

        historicalProductRepository.save(saved);
        log.info("Đã tạo snapshot lịch sử cho product {} tại {}", product.getId(), now);
    }

    @Transactional(readOnly = true)
    public HistoricalProduct findByProductAndDate(Long productId, LocalDateTime orderDate) {
        return historicalProductRepository.findByProductIdAndDate(productId, orderDate)
                .or(() -> historicalProductRepository.findEarliestByProductId(productId))
                .orElseThrow(() -> new AppException(ErrorCode.HISTORICAL_PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public boolean hasAnySnapshot(Long productId) {
        return historicalProductRepository.existsByProductId(productId);
    }
}

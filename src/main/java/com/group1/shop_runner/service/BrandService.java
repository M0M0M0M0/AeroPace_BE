package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.brand.request.BrandRequest;
import com.group1.shop_runner.dto.brand.response.BrandResponse;
import com.group1.shop_runner.entity.Brand;
import com.group1.shop_runner.repository.BrandRepository;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    // =========================================================
    // API 1: Lấy tất cả brand
    // GET /api/v1/brands
    // =========================================================
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 2: Lấy brand theo id
    // GET /api/v1/brands/{id}
    // =========================================================
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return mapToResponse(brand);
    }

    // =========================================================
    // API 3: Tạo mới brand
    // POST /api/v1/brands
    // =========================================================
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        brandRepository.findByName(request.getName())
                .ifPresent(b -> {
                    throw new AppException(ErrorCode.BRAND_NAME_ALREADY_EXISTS);
                });

        Brand brand = new Brand();
        brand.setName(request.getName());

        Brand saved = brandRepository.save(brand);
        return mapToResponse(saved);
    }

    // =========================================================
    // API 4: Cập nhật brand
    // PUT /api/v1/brands/{id}
    // =========================================================
    @Transactional
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        brandRepository.findByName(request.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new AppException(ErrorCode.BRAND_NAME_ALREADY_EXISTS);
                    }
                });

        brand.setName(request.getName());

        Brand updated = brandRepository.save(brand);
        return mapToResponse(updated);
    }

    // =========================================================
    // API 5: Xóa brand
    // DELETE /api/v1/brands/{id}
    // =========================================================
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
    }

    // =========================================================
    // MAPPER
    // =========================================================
    private BrandResponse mapToResponse(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName());
    }
}
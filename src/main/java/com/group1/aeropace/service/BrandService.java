package com.group1.aeropace.service;

import com.group1.aeropace.dto.brand.request.BrandRequest;
import com.group1.aeropace.dto.brand.response.BrandResponse;
import com.group1.aeropace.entity.Brand;
import com.group1.aeropace.repository.BrandRepository;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

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

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
    }

    private BrandResponse mapToResponse(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName());
    }
}

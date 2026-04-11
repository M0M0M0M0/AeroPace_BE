package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.brand.response.BrandResponse;
import com.group1.shop_runner.entity.Brand;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    // =========================================================
    // API 1: Lấy tất cả brand
    // =========================================================
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 2: Lấy brand theo id
    // =========================================================
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return mapToResponse(brand);
    }

    private BrandResponse mapToResponse(Brand brand) {
        BrandResponse res = new BrandResponse();
        res.setId(brand.getId());
        res.setName(brand.getName());
        return res;
    }
}
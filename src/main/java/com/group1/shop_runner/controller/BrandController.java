package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.brand.request.BrandRequest;
import com.group1.shop_runner.dto.brand.response.BrandResponse;
import com.group1.shop_runner.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    // =========================================================
    // API 1: Lấy tất cả brand
    // GET /api/v1/brands
    // =========================================================
    @GetMapping
    public List<BrandResponse> getAllBrands() {
        return brandService.getAllBrands();
    }

    // =========================================================
    // API 2: Lấy brand theo id
    // GET /api/v1/brands/{id}
    // =========================================================
    @GetMapping("/{id}")
    public BrandResponse getBrandById(@PathVariable Long id) {
        return brandService.getBrandById(id);
    }

    // =========================================================
    // API 3: Tạo mới brand
    // POST /api/v1/brands
    // =========================================================
    @PostMapping
    public BrandResponse createBrand(@Valid @RequestBody BrandRequest request) {
        return brandService.createBrand(request);
    }

    // =========================================================
    // API 4: Cập nhật brand
    // PUT /api/v1/brands/{id}
    // =========================================================
    @PutMapping("/{id}")
    public BrandResponse updateBrand(@PathVariable Long id,
                                     @Valid @RequestBody BrandRequest request) {
        return brandService.updateBrand(id, request);
    }

    // =========================================================
    // API 5: Xóa brand
    // DELETE /api/v1/brands/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public String deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return "Delete brand successfully";
    }
}
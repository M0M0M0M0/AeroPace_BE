package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.brand.response.BrandResponse;
import com.group1.shop_runner.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
//@CrossOrigin(origins = "*")
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
}
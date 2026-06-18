package com.group1.aeropace.controller;

import com.group1.aeropace.dto.product.request.ProductFullUpdateRequest;
import com.group1.aeropace.dto.product.response.ProductResponse;
import com.group1.aeropace.entity.Product;
import com.group1.aeropace.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/filter")
    public Map<String, Object> filterProductsForAdmin(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> brands,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<Product.Status> statuses,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Integer stockMin,
            @RequestParam(required = false) Integer stockMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) BigDecimal ratingMin,
            @RequestParam(required = false) BigDecimal ratingMax,
            @RequestParam(required = false) Integer reviewCountMin,
            @RequestParam(required = false) Integer reviewCountMax,
            @RequestParam(required = false) Boolean sortByBestSeller,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Integer limit
    ) {
        return productService.filterProductsForAdmin(
                name, brands, categories, minPrice, maxPrice, statuses,
                productId, variantId, sku, stockMin, stockMax, page, ratingMin, ratingMax,
                reviewCountMin, reviewCountMax, sortByBestSeller, dateFrom, dateTo, limit
        );
    }

    @PostMapping("/full-create")
    public ResponseEntity<ProductResponse> fullCreateProduct(
            @RequestBody ProductFullUpdateRequest request
    ) {
        ProductResponse result = productService.fullCreateProduct(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/full-update")
    public ResponseEntity<ProductResponse> fullUpdateProduct(
            @PathVariable Long id,
            @RequestBody ProductFullUpdateRequest request
    ) {
        ProductResponse result = productService.fullUpdateProduct(id, request);
        return ResponseEntity.ok(result);
    }
}

package com.group1.shop_runner.dto.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal minPrice;
    private List<String> images;
    private List<ProductVariantResponse> variants;
    private String brandName;
}
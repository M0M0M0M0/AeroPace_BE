package com.group1.shop_runner.dto.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryResponse {
    private Long productId;
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
}
package com.group1.shop_runner.dto.product.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductListResponse {
    private List<ProductResponse> products;
}
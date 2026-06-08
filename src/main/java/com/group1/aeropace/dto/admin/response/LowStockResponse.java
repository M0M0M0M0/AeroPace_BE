package com.group1.aeropace.dto.admin.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LowStockResponse {
    private Long variantId;
    private String productName;
    private String sku;
    private int stock;
}

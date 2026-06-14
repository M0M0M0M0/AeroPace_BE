package com.group1.aeropace.dto.order.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponse {
    private Long productId;
    private Long productVariantId;
    private Integer quantity;
    private BigDecimal price;

    private String productName;
    private String variantName;
    private String sku;
    private String productImgUrl;
    private String brandName;
    private String note;
}
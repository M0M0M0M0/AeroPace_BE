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
    private String option1_value;
    private String option2_value;
    private String option3_value;
    private String sku;
    private String productImgUrl;
    private String note;
}
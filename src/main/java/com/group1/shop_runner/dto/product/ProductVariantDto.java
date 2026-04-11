package com.group1.shop_runner.dto.product;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductVariantDto {
    private Long productId;
    private Long id;
    private String option1Value;
    private String option2Value;
    private String option3Value;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private Integer stock;
    private String sku;

    public ProductVariantDto(Long productId, Long id, String option1Value,
                             String option2Value, String option3Value,
                             BigDecimal price, BigDecimal comparePrice,
                             Integer stock, String sku) {
        this.productId = productId;
        this.id = id;
        this.option1Value = option1Value;
        this.option2Value = option2Value;
        this.option3Value = option3Value;
        this.price = price;
        this.comparePrice = comparePrice;
        this.stock = stock;
        this.sku = sku;
    }
}

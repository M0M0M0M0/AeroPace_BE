package com.group1.shop_runner.dto.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data   // @Data là lombok tạo sẵn Getter, Setter, toString .... chứ không tạo sẵn Constructor nhé
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String option1Value;
    private String option2Value;
    private String option3Value;
    private BigDecimal price;
    private Integer stock;
}

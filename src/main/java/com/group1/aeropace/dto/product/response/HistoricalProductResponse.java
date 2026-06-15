package com.group1.aeropace.dto.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalProductResponse {
    private Long id;
    private Long productId;
    private String name;
    private String description;
    private String brand;
    private String option1Name;
    private String option2Name;
    private String option3Name;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private List<ImageDto> images;
    private List<VariantDto> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private Long id;
        private String imageUrl;
        private Integer position;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDto {
        private Long id;
        private Long originalVariantId;
        private String sku;
        private BigDecimal price;
        private BigDecimal comparePrice;
        private String option1Value;
        private String option2Value;
        private String option3Value;
    }
}

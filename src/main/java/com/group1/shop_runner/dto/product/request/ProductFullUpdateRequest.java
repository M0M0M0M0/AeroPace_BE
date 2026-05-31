package com.group1.shop_runner.dto.product.request;

import com.group1.shop_runner.entity.Product;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductFullUpdateRequest {

    private String name;
    private String description;
    private Long brandId;
    private String option1Name;
    private String option2Name;
    private String option3Name;
    private Product.Status status;

    private List<VariantItem> variants;

    private List<ImageItem> images;

    private List<Long> categoryIds;

    @Getter
    @Setter
    public static class VariantItem {
        private Long id;
        private String option1Value;
        private String option2Value;
        private String option3Value;
        private BigDecimal price;
        private Integer stock;
        private String sku;
        private Boolean isDeleted;
    }

    @Getter
    @Setter
    public static class ImageItem {
        private Long id;
        private String imageUrl;
        private Integer position;
    }
}
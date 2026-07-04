package com.group1.aeropace.dto.product.request;

import com.group1.aeropace.entity.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductFullUpdateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private String description;

    @NotNull(message = "Brand is required")
    private Long brandId;

    @Size(max = 50, message = "Option 1 name must not exceed 50 characters")
    private String option1Name;

    @Size(max = 50, message = "Option 2 name must not exceed 50 characters")
    private String option2Name;

    @Size(max = 50, message = "Option 3 name must not exceed 50 characters")
    private String option3Name;

    private Product.Status status;

    @Valid
    private List<VariantItem> variants;

    @Valid
    private List<ImageItem> images;

    private List<Long> categoryIds;

    @Getter
    @Setter
    public static class VariantItem {
        private Long id;

        @Size(max = 50, message = "Option 1 value must not exceed 50 characters")
        private String option1Value;

        @Size(max = 50, message = "Option 2 value must not exceed 50 characters")
        private String option2Value;

        @Size(max = 50, message = "Option 3 value must not exceed 50 characters")
        private String option3Value;

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        private BigDecimal price;

        @Min(value = 0, message = "Stock must not be negative")
        private Integer stock;

        @Size(max = 100, message = "SKU must not exceed 100 characters")
        private String sku;

        private Boolean isDeleted;
    }

    @Getter
    @Setter
    public static class ImageItem {
        private Long id;

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        private String imageUrl;

        @Min(value = 1, message = "Position must be greater than 0")
        private Integer position;
    }
}
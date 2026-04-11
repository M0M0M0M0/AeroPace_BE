package com.group1.shop_runner.dto.product.response;

import com.group1.shop_runner.dto.category.CategoryDto;
import com.group1.shop_runner.dto.product.ProductImageDto;
import com.group1.shop_runner.dto.product.ProductVariantDto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String brand;

    private String option1Name;
    private String option2Name;
    private String option3Name;

    private List<ProductImageDto> images;
    private List<ProductVariantDto> variants;
    private List<CategoryDto> categories;

    //     constructor dùng cho JPQL
    public ProductResponse(Long id, String name, String slug, String description,
                           String brand, String option1Name, String option2Name, String option3Name) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.brand = brand;
        this.option1Name = option1Name;
        this.option2Name = option2Name;
        this.option3Name = option3Name;
    }
}

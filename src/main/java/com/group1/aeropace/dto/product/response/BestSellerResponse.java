package com.group1.aeropace.dto.product.response;

import com.group1.aeropace.dto.category.CategoryDto;
import com.group1.aeropace.dto.product.ProductImageDto;
import com.group1.aeropace.dto.product.ProductVariantDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BestSellerResponse {
    private Long id;
    private String name;
    private String brand;
    private String status;
    private Long totalSold;
    private List<ProductImageDto> images;
    private List<ProductVariantDto> variants;
    private List<CategoryDto> categories;
}
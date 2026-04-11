package com.group1.shop_runner.dto.product.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    // Tiếp nhận thông tin từ client gửi lên
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    @NotNull(message = "Brand is required")
    private Long brandId;
    private String option1Name;
    private String option2Name;
    private String option3Name;
}

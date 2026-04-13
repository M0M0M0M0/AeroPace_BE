package com.group1.shop_runner.dto.brand.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandRequest {

    @NotBlank(message = "Tên brand không được để trống")
    @Size(max = 50, message = "Tên brand không được vượt quá 50 ký tự")
    private String name;
}
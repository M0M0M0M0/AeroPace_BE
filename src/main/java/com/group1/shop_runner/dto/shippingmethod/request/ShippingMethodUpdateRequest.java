package com.group1.shop_runner.dto.shippingmethod.request;

import com.group1.shop_runner.entity.ShippingMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingMethodUpdateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal fee;

    @NotNull
    private ShippingMethod.Status status;
}

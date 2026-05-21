package com.group1.shop_runner.dto.shippingmethod.request;

import com.group1.shop_runner.entity.ShippingMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingMethodRequest {


    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal fee;

}

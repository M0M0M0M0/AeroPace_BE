package com.group1.aeropace.dto.shippingmethod.request;

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

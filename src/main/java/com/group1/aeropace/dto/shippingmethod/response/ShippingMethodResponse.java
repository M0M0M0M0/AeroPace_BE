package com.group1.aeropace.dto.shippingmethod.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingMethodResponse {

    private Long id;
    private String name;
    private BigDecimal fee;
    private String status;
}

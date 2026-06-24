package com.group1.aeropace.dto.admin.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopBuyerResponse {
    private Long userId;
    private String fullName;
    private String email;
    private Long orderCount;
    private BigDecimal totalSpent;
}

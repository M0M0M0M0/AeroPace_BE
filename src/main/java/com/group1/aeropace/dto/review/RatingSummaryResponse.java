package com.group1.aeropace.dto.review;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class RatingSummaryResponse {

    private BigDecimal averageRating;
    private Integer reviewCount;
    private Map<BigDecimal, Long> distribution;
}
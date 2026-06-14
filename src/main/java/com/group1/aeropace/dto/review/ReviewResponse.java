package com.group1.aeropace.dto.review;

import com.group1.aeropace.enums.ReviewStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private String variantName;
    private BigDecimal rating;
    private String comment;
    private List<String> imageUrls;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private boolean canEdit;
}
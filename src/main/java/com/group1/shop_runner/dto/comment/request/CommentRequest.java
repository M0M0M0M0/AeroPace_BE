package com.group1.shop_runner.dto.comment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "User is required")
    private Long userId;

    @NotNull(message = "Product is required")
    private Long productId;

    @NotBlank(message = "Content is required")
    private String content;
}
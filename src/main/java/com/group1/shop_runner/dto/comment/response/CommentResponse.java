package com.group1.shop_runner.dto.comment.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {

    private Long id;

    private Long userId;
    private String username;

    private Long productId;
    private String productName;

    private String content;
}
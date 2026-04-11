package com.group1.shop_runner.dto.post.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String thumbnail;

    private Long userId;
    private String username;
}
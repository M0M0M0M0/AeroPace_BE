package com.group1.aeropace.dto.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDeleteReviewRequest {

    @NotBlank(message = "Note (reason) is required for admin deletion")
    private String note;
}
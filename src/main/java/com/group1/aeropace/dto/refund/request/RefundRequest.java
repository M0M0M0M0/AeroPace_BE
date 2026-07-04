package com.group1.aeropace.dto.refund.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundRequest {
    @NotBlank(message = "Refund reason is required")
    @Size(max = 1000, message = "Refund reason must not exceed 1000 characters")
    private String refundReason;
}

package com.group1.aeropace.dto.order.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CheckoutRequest {
    @NotNull(message = "User is required")
    private Long userId;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]+$", message = "Phone number must be numeric")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    private String paymentMethod;
    private String paymentOrderId;

    @Size(max = 255, message = "Receiver name must not exceed 255 characters")
    private String receiverName;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String ward;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;

    private BigDecimal vat;

    @NotNull(message = "Shipping method is required")
    private Long shippingMethodId;

    @NotNull(message = "Grand total is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Grand total must be greater than 0")
    private BigDecimal grandTotal;
}
package com.group1.shop_runner.dto.customerprofile.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerProfileRequest {
    private Long userId;
    private String fullName;
    private String address;
    @Past(message = "DOB must be in the past")
    private LocalDate dob;
    @Pattern(regexp = "^[0-9]+$", message = "Phone must be numeric")
    private String phoneNumber;
    private String gender;
}
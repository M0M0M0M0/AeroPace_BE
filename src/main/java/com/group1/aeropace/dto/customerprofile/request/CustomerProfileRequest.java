package com.group1.aeropace.dto.customerprofile.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerProfileRequest {
    private Long userId;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 150, message = "Ward must not exceed 150 characters")
    private String ward;

    @Size(max = 150, message = "District must not exceed 150 characters")
    private String district;

    @Size(max = 150, message = "Province must not exceed 150 characters")
    private String province;

    @Past(message = "DOB must be in the past")
    private LocalDate dob;

    @Pattern(regexp = "^[0-9]+$", message = "Phone must be numeric")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    private String gender;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;
}
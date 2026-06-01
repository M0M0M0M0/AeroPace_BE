package com.group1.aeropace.dto.customerprofile.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerProfileRequest {
    private Long userId;
    private String fullName;
    private String address;
    private String ward;
    private String district;
    private String province;
    @Past(message = "DOB must be in the past")
    private LocalDate dob;
    @Pattern(regexp = "^[0-9]+$", message = "Phone must be numeric")
    private String phoneNumber;
    private String gender;
}
package com.group1.shop_runner.dto.admin.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdminCustomerUpdateRequest {

    // User fields
    private String email;

    // CustomerProfile fields
    private String fullName;
    private String phoneNumber;
    private String address;
    private String gender;
    private LocalDate dob;
}
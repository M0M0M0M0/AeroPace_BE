package com.group1.shop_runner.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminCustomerResponse {

    // User fields
    private Long userId;
    private String username;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    // CustomerProfile fields
    private Long profileId;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String gender;
    private LocalDate dob;
}
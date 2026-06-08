package com.group1.aeropace.dto.admin.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminCustomerDetailResponse {
    // User info
    private Long id;
    private String username;
    private String email;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Profile info
    private String fullName;
    private String phoneNumber;
    private String gender;
    private LocalDate dob;

    // Address
    private String address;
    private String ward;
    private String district;
    private String province;

    // Profile timestamps
    private LocalDateTime profileCreatedAt;
    private LocalDateTime profileUpdatedAt;
}

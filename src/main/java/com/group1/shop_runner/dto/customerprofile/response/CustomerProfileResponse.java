package com.group1.shop_runner.dto.customerprofile.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String address;
    private LocalDate dob;
    private String phoneNumber;
    private String gender;
}
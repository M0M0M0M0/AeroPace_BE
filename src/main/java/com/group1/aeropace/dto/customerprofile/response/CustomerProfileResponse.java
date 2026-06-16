package com.group1.aeropace.dto.customerprofile.response;

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
    private String ward;
    private String district;
    private String province;
    private LocalDate dob;
    private String phoneNumber;
    private String gender;
    private String avatarUrl;
}
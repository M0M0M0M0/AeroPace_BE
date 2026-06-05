package com.group1.aeropace.dto.admindashboard.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewCustomerResponse {
    private Long userId;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
}

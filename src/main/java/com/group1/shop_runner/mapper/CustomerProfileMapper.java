package com.group1.shop_runner.mapper;

import com.group1.shop_runner.dto.customerprofile.response.CustomerProfileResponse;
import com.group1.shop_runner.entity.CustomerProfile;
import org.springframework.stereotype.Component;

@Component
public class CustomerProfileMapper {

    public CustomerProfileResponse toResponse(CustomerProfile profile) {
        return new CustomerProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getFullName(),
                profile.getAddress(),
                profile.getDob(),
                profile.getPhoneNumber(),
                profile.getGender()
        );
    }
}
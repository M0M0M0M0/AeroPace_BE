package com.group1.aeropace.mapper;

import com.group1.aeropace.dto.customerprofile.response.CustomerProfileResponse;
import com.group1.aeropace.entity.CustomerProfile;
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
                profile.getProvince(),
                profile.getWard(),
                profile.getDistrict(),
                profile.getDob(),
                profile.getPhoneNumber(),
                profile.getGender(),
                profile.getAvatarUrl()
        );
    }
}
package com.group1.aeropace.service;

import com.group1.aeropace.dto.customerprofile.request.CustomerProfileRequest;
import com.group1.aeropace.dto.customerprofile.response.CustomerProfileResponse;
import com.group1.aeropace.entity.CustomerProfile;
import com.group1.aeropace.entity.User;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.repository.CustomerProfileRepository;
import com.group1.aeropace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomerProfileService {

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public CustomerProfileResponse getByUserId(Long userId) {
        CustomerProfile profile = customerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));
        return mapToResponse(profile);
    }

    @Transactional
    public CustomerProfileResponse updateCustomerProfile(Long id, CustomerProfileRequest request) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        profile.setFullName(request.getFullName());
        profile.setAddress(request.getAddress());
        profile.setWard(request.getWard());
        profile.setDistrict(request.getDistrict());
        profile.setProvince(request.getProvince());
        profile.setDob(request.getDob());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setUpdatedAt(LocalDateTime.now());

        CustomerProfile updated = customerProfileRepository.save(profile);
        return mapToResponse(updated);
    }

    private CustomerProfileResponse mapToResponse(CustomerProfile profile) {
        return new CustomerProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getFullName(),
                profile.getAddress(),
                profile.getWard(),
                profile.getDistrict(),
                profile.getProvince(),
                profile.getDob(),
                profile.getPhoneNumber(),
                profile.getGender(),
                profile.getAvatarUrl()
        );
    }
}

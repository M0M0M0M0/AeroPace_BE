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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomerProfileService {
    private static final Logger log =
            LoggerFactory.getLogger(CustomerProfileService.class);

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CustomerProfileResponse createCustomerProfile(CustomerProfileRequest request) {
        if (request.getUserId() == null || request.getFullName() == null || request.getFullName().isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        customerProfileRepository.findByUser_Id(request.getUserId())
                .ifPresent(profile -> {
                    throw new AppException(ErrorCode.CUSTOMER_PROFILE_ALREADY_EXISTS);
                });

        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        profile.setFullName(request.getFullName());
        profile.setAddress(request.getAddress());
        profile.setWard(request.getWard());
        profile.setDistrict(request.getDistrict());
        profile.setProvince(request.getProvince());
        profile.setDob(request.getDob());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        CustomerProfile saved = customerProfileRepository.save(profile);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerProfileResponse> getAllCustomerProfiles() {
        return customerProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerProfileById(Long id) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerProfileByUserId(Long userId) {
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

    @Transactional
    public void deleteCustomerProfile(Long id) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        customerProfileRepository.delete(profile);
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
    //lay profile theo user_id
    @Transactional(readOnly = true)
    public CustomerProfileResponse getByUserId(Long userId) {
        CustomerProfile profile = customerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        return mapToResponse(profile);
    }
}

package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.customerprofile.request.CustomerProfileRequest;
import com.group1.shop_runner.dto.customerprofile.response.CustomerProfileResponse;
import com.group1.shop_runner.entity.CustomerProfile;
import com.group1.shop_runner.entity.User;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.CustomerProfileRepository;
import com.group1.shop_runner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerProfileService {

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================================================
    // API 1: Tạo customer profile
    // =========================================================
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
        profile.setDob(request.getDob());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        CustomerProfile saved = customerProfileRepository.save(profile);

        return mapToResponse(saved);
    }

    // =========================================================
    // API 2: Lấy tất cả customer profile
    // =========================================================
    public List<CustomerProfileResponse> getAllCustomerProfiles() {
        return customerProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 3: Lấy customer profile theo id
    // =========================================================
    public CustomerProfileResponse getCustomerProfileById(Long id) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));
        System.out.println("GET PROFILE ID = " + id);
        return mapToResponse(profile);
    }

    // =========================================================
    // API 4: Lấy customer profile theo userId
    // =========================================================
    public CustomerProfileResponse getCustomerProfileByUserId(Long userId) {
        CustomerProfile profile = customerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        return mapToResponse(profile);
    }

    // =========================================================
    // API 5: Cập nhật customer profile
    // =========================================================
    public CustomerProfileResponse updateCustomerProfile(Long id, CustomerProfileRequest request) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        profile.setFullName(request.getFullName());
        profile.setAddress(request.getAddress());
        profile.setDob(request.getDob());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setGender(request.getGender());
        profile.setUpdatedAt(LocalDateTime.now());

        CustomerProfile updated = customerProfileRepository.save(profile);

        return mapToResponse(updated);
    }

    // =========================================================
    // API 6: Xóa customer profile
    // =========================================================
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
                profile.getDob(),
                profile.getPhoneNumber(),
                profile.getGender()
        );
    }
    //lay profile theo user_id
    public CustomerProfileResponse getByUserId(Long userId) {
        CustomerProfile profile = customerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        return mapToResponse(profile);
    }
}
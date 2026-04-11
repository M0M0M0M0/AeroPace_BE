package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.admin.request.AdminCustomerUpdateRequest;
import com.group1.shop_runner.dto.admin.response.AdminCustomerResponse;
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
public class AdminCustomerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    // =========================================================
    // API 1: Lấy danh sách tất cả khách hàng (role = USER)
    // =========================================================
    public List<AdminCustomerResponse> getAllCustomers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != null &&
                        u.getRole().getName().equalsIgnoreCase("USER"))
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 2: Lấy chi tiết khách hàng theo userId
    // =========================================================
    public AdminCustomerResponse getCustomerById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return mapToResponse(user);
    }

    // =========================================================
    // API 3: Khoá / Mở khoá tài khoản (toggle status)
    // =========================================================
    public AdminCustomerResponse toggleLockCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newStatus = "ACTIVE".equalsIgnoreCase(user.getStatus()) ? "LOCKED" : "ACTIVE";
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return mapToResponse(user);
    }

    // =========================================================
    // API 4: Cập nhật thông tin khách hàng (email + profile)
    // =========================================================
    public AdminCustomerResponse updateCustomer(Long userId, AdminCustomerUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Cập nhật email trên bảng User
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        // Cập nhật CustomerProfile
        CustomerProfile profile = customerProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getDob() != null) {
            profile.setDob(request.getDob());
        }
        profile.setUpdatedAt(LocalDateTime.now());
        customerProfileRepository.save(profile);

        return mapToResponse(user);
    }

    // =========================================================
    // Helper: map User + CustomerProfile → AdminCustomerResponse
    // =========================================================
    private AdminCustomerResponse mapToResponse(User user) {
        CustomerProfile profile = customerProfileRepository
                .findByUser_Id(user.getId())
                .orElse(null);

        return new AdminCustomerResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt(),
                profile != null ? profile.getId()          : null,
                profile != null ? profile.getFullName()    : "",
                profile != null ? profile.getPhoneNumber() : "",
                profile != null ? profile.getAddress()     : "",
                profile != null ? profile.getGender()      : "",
                profile != null ? profile.getDob()         : null
        );
    }
}
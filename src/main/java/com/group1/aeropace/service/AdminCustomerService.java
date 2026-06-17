package com.group1.aeropace.service;

import com.group1.aeropace.dto.admin.request.AdminCustomerUpdateRequest;
import com.group1.aeropace.dto.admin.response.AdminCustomerDetailResponse;
import com.group1.aeropace.dto.admin.response.AdminCustomerResponse;
import com.group1.aeropace.entity.CustomerProfile;
import com.group1.aeropace.entity.User;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.repository.CustomerProfileRepository;
import com.group1.aeropace.repository.UserRepository;
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

    public List<AdminCustomerResponse> getAllCustomers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != null &&
                        u.getRole().getName().equalsIgnoreCase("USER"))
                .map(this::mapToResponse)
                .toList();
    }

    public AdminCustomerDetailResponse getCustomerDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CustomerProfile profile = customerProfileRepository
                .findByUser_Id(userId)
                .orElse(null);

        return AdminCustomerDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .fullName(profile != null ? profile.getFullName() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .gender(profile != null ? profile.getGender() : null)
                .dob(profile != null ? profile.getDob() : null)
                .address(profile != null ? profile.getAddress() : null)
                .ward(profile != null ? profile.getWard() : null)
                .district(profile != null ? profile.getDistrict() : null)
                .province(profile != null ? profile.getProvince() : null)
                .profileCreatedAt(profile != null ? profile.getCreatedAt() : null)
                .profileUpdatedAt(profile != null ? profile.getUpdatedAt() : null)
                .build();
    }

    public AdminCustomerResponse toggleLockCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newStatus = "ACTIVE".equalsIgnoreCase(user.getStatus()) ? "LOCKED" : "ACTIVE";
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return mapToResponse(user);
    }

    public AdminCustomerResponse updateCustomer(Long userId, AdminCustomerUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

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

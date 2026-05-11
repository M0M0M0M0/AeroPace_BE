package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.user.request.UserRequest;
import com.group1.shop_runner.dto.user.response.UserResponse;
import com.group1.shop_runner.entity.Role;
import com.group1.shop_runner.entity.User;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.RoleRepository;
import com.group1.shop_runner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.group1.shop_runner.repository.CustomerProfileRepository;
import com.group1.shop_runner.entity.CustomerProfile;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    // =========================================================
    // API 1: Tạo user
    // =========================================================
    public UserResponse createUser(UserRequest request) {

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
                });

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        // =========================================
        // AUTO CREATE CUSTOMER PROFILE
        // =========================================
        if (saved.getRole() != null &&
                saved.getRole().getName().equalsIgnoreCase("USER")) {
            CustomerProfile profile = new CustomerProfile();

            profile.setUser(saved);

            profile.setFullName("");
            profile.setAddress("");
            profile.setPhoneNumber("");
            profile.setGender("");
            profile.setDob(null);

            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());

            customerProfileRepository.save(profile);
        }

        return mapToResponse(saved);
    }

    // =========================================================
    // API 2: Lấy tất cả user
    // =========================================================
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 3: Lấy user theo id
    // =========================================================
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return mapToResponse(user);
    }

    // =========================================================
    // API 4: Update user
    // =========================================================
    public UserResponse updateUser(Long id, UserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);

        return mapToResponse(updated);
    }

    // =========================================================
    // API 5: Delete user
    // =========================================================
//    public void deleteUser(Long id) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        userRepository.delete(user);
//    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName()
        );
    }
}
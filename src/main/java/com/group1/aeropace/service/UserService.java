package com.group1.aeropace.service;

import com.group1.aeropace.dto.user.request.UserRequest;
import com.group1.aeropace.dto.user.response.UserResponse;
import com.group1.aeropace.entity.Role;
import com.group1.aeropace.entity.User;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.repository.RoleRepository;
import com.group1.aeropace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.group1.aeropace.repository.CustomerProfileRepository;
import com.group1.aeropace.entity.CustomerProfile;
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

        // Tự động tạo CustomerProfile trống cho role USER
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

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return mapToResponse(user);
    }

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



    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getName()
        );
    }
}

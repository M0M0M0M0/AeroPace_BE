package com.group1.shop_runner.service;

import com.group1.shop_runner.config.JwtUtil;
import com.group1.shop_runner.dto.auth.request.RegisterRequest;
import com.group1.shop_runner.dto.auth.response.LoginResponse;
import com.group1.shop_runner.entity.CustomerProfile;
import com.group1.shop_runner.entity.Role;
import com.group1.shop_runner.entity.User;
import com.group1.shop_runner.repository.CustomerProfileRepository;
import com.group1.shop_runner.repository.RoleRepository;
import com.group1.shop_runner.repository.UserRepository;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().getName()
        );

        return new LoginResponse(
                user.getId(),
                token,
                user.getUsername(),
                user.getRole().getName(),
                user.getStatus()
        );
    }
    public String register(RegisterRequest request) {

        // Check username/email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        //set status default la active
        user.setStatus("ACTIVE");

        // encode password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(userRole);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        // CREATE PROFILE
        User savedUser = userRepository.save(user);
        CustomerProfile profile = new CustomerProfile();
        profile.setUser(savedUser);

        profile.setFullName("");
        profile.setAddress("");
        profile.setPhoneNumber("");
        profile.setGender("");
        profile.setDob(null);

        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        customerProfileRepository.save(profile);

        return "Register success";
    }
}
package com.group1.aeropace.controller;

import com.group1.aeropace.dto.auth.request.LoginRequest;
import com.group1.aeropace.dto.auth.request.RegisterRequest;
import com.group1.aeropace.dto.auth.response.LoginResponse;
import com.group1.aeropace.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
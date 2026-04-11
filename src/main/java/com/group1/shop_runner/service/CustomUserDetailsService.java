package com.group1.shop_runner.service;

import com.group1.shop_runner.config.CustomUserDetails;
import com.group1.shop_runner.entity.User;
import com.group1.shop_runner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
    public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {

//        System.out.println("LOAD USER BY USERNAME: " + username);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    System.out.println(" USER NOT FOUND IN DB");
                    return new RuntimeException("User not found");
                });

//        System.out.println("FOUND USER: " + user.getEmail());

        return new CustomUserDetails(user);
    }
    }


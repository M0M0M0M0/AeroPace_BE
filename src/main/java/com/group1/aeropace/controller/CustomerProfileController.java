package com.group1.aeropace.controller;

import com.group1.aeropace.dto.customerprofile.request.CustomerProfileRequest;
import com.group1.aeropace.dto.customerprofile.response.CustomerProfileResponse;
import com.group1.aeropace.service.CustomerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer-profiles")
public class CustomerProfileController {

    @Autowired
    private CustomerProfileService customerProfileService;

    @GetMapping("/user/{userId}")
    public CustomerProfileResponse getByUserId(@PathVariable Long userId) {
        return customerProfileService.getByUserId(userId);
    }

    @PutMapping("/{id}")
    public CustomerProfileResponse updateCustomerProfile(@PathVariable Long id,
                                                         @RequestBody CustomerProfileRequest request) {
        return customerProfileService.updateCustomerProfile(id, request);
    }
}

package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.customerprofile.request.CustomerProfileRequest;
import com.group1.shop_runner.dto.customerprofile.response.CustomerProfileResponse;
import com.group1.shop_runner.service.CustomerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-profiles")
//@CrossOrigin(origins = "*")
public class CustomerProfileController {

    @Autowired
    private CustomerProfileService customerProfileService;

    // =========================================================
    // API 1: Tạo customer profile
    // POST /api/v1/customer-profiles
    // =========================================================
    @PostMapping
    public CustomerProfileResponse createCustomerProfile(@RequestBody CustomerProfileRequest request) {
        return customerProfileService.createCustomerProfile(request);
    }

    // =========================================================
    // API 2: Lấy tất cả customer profile
    // GET /api/v1/customer-profiles
    // =========================================================
    @GetMapping
    public List<CustomerProfileResponse> getAllCustomerProfiles() {
        return customerProfileService.getAllCustomerProfiles();
    }

    // =========================================================
    // API 3: Lấy customer profile theo id
    // GET /api/v1/customer-profiles/{id}
    // =========================================================
    @GetMapping("/{id}")
    public CustomerProfileResponse getCustomerProfileById(@PathVariable Long id) {
        return customerProfileService.getCustomerProfileById(id);
    }

    // =========================================================
    // API 4: Lấy customer profile theo userId
    // GET /api/v1/customer-profiles/user/{userId}
    // =========================================================
    @GetMapping("/user/{userId}")
    public CustomerProfileResponse getByUserId(@PathVariable Long userId) {
        return customerProfileService.getByUserId(userId);
    }

    // =========================================================
    // API 5: Cập nhật customer profile
    // PUT /api/v1/customer-profiles/{id}
    // =========================================================
    @PutMapping("/{id}")
    public CustomerProfileResponse updateCustomerProfile(@PathVariable Long id,
                                                         @RequestBody CustomerProfileRequest request) {
        return customerProfileService.updateCustomerProfile(id, request);
    }

    // =========================================================
    // API 6: Xóa customer profile
    // DELETE /api/v1/customer-profiles/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public String deleteCustomerProfile(@PathVariable Long id) {
        customerProfileService.deleteCustomerProfile(id);
        return "Delete customer profile successfully";
    }

}
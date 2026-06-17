package com.group1.aeropace.controller;

import com.group1.aeropace.dto.admin.request.AdminCustomerUpdateRequest;
import com.group1.aeropace.dto.admin.response.AdminCustomerDetailResponse;
import com.group1.aeropace.dto.admin.response.AdminCustomerResponse;
import com.group1.aeropace.service.AdminCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    @Autowired
    private AdminCustomerService adminCustomerService;

    // GET /api/v1/admin/customers
    @GetMapping
    public List<AdminCustomerResponse> getAllCustomers() {
        return adminCustomerService.getAllCustomers();
    }

    // GET /api/v1/admin/customers/{userId}
    @GetMapping("/{userId}")
    public AdminCustomerDetailResponse getCustomerDetail(@PathVariable Long userId) {
        return adminCustomerService.getCustomerDetail(userId);
    }

    // PATCH /api/v1/admin/customers/{userId}/toggle-lock
    @PatchMapping("/{userId}/toggle-lock")
    public AdminCustomerResponse toggleLockCustomer(@PathVariable Long userId) {
        return adminCustomerService.toggleLockCustomer(userId);
    }

    // PUT /api/v1/admin/customers/{userId}
    @PutMapping("/{userId}")
    public AdminCustomerResponse updateCustomer(
            @PathVariable Long userId,
            @RequestBody AdminCustomerUpdateRequest request
    ) {
        return adminCustomerService.updateCustomer(userId, request);
    }
}

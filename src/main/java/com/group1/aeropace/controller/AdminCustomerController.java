package com.group1.aeropace.controller;

import com.group1.aeropace.dto.admin.response.AdminCustomerDetailResponse;
import com.group1.aeropace.dto.admin.response.AdminCustomerResponse;
import com.group1.aeropace.service.AdminCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    @Autowired
    private AdminCustomerService adminCustomerService;

    @GetMapping
    public List<AdminCustomerResponse> getAllCustomers() {
        return adminCustomerService.getAllCustomers();
    }

    @GetMapping("/{userId}")
    public AdminCustomerDetailResponse getCustomerDetail(@PathVariable Long userId) {
        return adminCustomerService.getCustomerDetail(userId);
    }

    @PatchMapping("/{userId}/toggle-lock")
    public AdminCustomerResponse toggleLockCustomer(@PathVariable Long userId) {
        return adminCustomerService.toggleLockCustomer(userId);
    }
}

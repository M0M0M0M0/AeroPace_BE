package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.admin.request.AdminCustomerUpdateRequest;
import com.group1.shop_runner.dto.admin.response.AdminCustomerResponse;
import com.group1.shop_runner.service.AdminCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    @Autowired
    private AdminCustomerService adminCustomerService;

    // =========================================================
    // API 1: Lấy danh sách tất cả khách hàng
    // GET /api/v1/admin/customers
    // =========================================================
    @GetMapping
    public List<AdminCustomerResponse> getAllCustomers() {
        return adminCustomerService.getAllCustomers();
    }

    // =========================================================
    // API 2: Lấy chi tiết khách hàng theo userId
    // GET /api/v1/admin/customers/{userId}
    // =========================================================
    @GetMapping("/{userId}")
    public AdminCustomerResponse getCustomerById(@PathVariable Long userId) {
        return adminCustomerService.getCustomerById(userId);
    }

    // =========================================================
    // API 3: Khoá / Mở khoá tài khoản
    // PATCH /api/v1/admin/customers/{userId}/toggle-lock
    // =========================================================
    @PatchMapping("/{userId}/toggle-lock")
    public AdminCustomerResponse toggleLockCustomer(@PathVariable Long userId) {
        return adminCustomerService.toggleLockCustomer(userId);
    }

    // =========================================================
    // API 4: Cập nhật thông tin khách hàng
    // PUT /api/v1/admin/customers/{userId}
    // =========================================================
    @PutMapping("/{userId}")
    public AdminCustomerResponse updateCustomer(
            @PathVariable Long userId,
            @RequestBody AdminCustomerUpdateRequest request
    ) {
        return adminCustomerService.updateCustomer(userId, request);
    }
}
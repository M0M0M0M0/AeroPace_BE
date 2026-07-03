package com.group1.aeropace.controller;

import com.group1.aeropace.dto.admin.response.AdminCustomerDetailResponse;
import com.group1.aeropace.dto.admin.response.AdminCustomerResponse;
import com.group1.aeropace.service.AdminCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    @Autowired
    private AdminCustomerService adminCustomerService;

    @GetMapping
    public Object getAllCustomers(
            @RequestParam(required = false) String searchId,
            @RequestParam(required = false) String searchName,
            @RequestParam(required = false) String searchEmail,
            @RequestParam(required = false) String searchPhone,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page == null) {
            return adminCustomerService.getAllCustomers();
        }
        return adminCustomerService.getAllCustomersPaged(
                searchId, searchName, searchEmail, searchPhone, status, dateFrom, dateTo,
                page, size != null ? size : 20
        );
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

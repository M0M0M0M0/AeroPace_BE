package com.group1.aeropace.controller;

import com.group1.aeropace.dto.admin.response.LowStockResponse;
import com.group1.aeropace.dto.admin.response.NewCustomerResponse;
import com.group1.aeropace.dto.admin.response.TopBuyerResponse;
import com.group1.aeropace.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/low-stock")
    public List<LowStockResponse> getLowStock(
            @RequestParam(defaultValue = "5")  int threshold,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.getLowStock(threshold, limit);
    }

    @GetMapping("/new-customers")
    public List<NewCustomerResponse> getNewCustomers(
            @RequestParam String dateFrom,
            @RequestParam String dateTo
    ) {
        return dashboardService.getNewCustomers(dateFrom, dateTo);
    }

    @GetMapping("/top-buyers")
    public List<TopBuyerResponse> getTopBuyers(
            @RequestParam String dateFrom,
            @RequestParam String dateTo,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.getTopBuyers(dateFrom, dateTo, limit);
    }
}

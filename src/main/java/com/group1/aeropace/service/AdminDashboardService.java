package com.group1.aeropace.service;

import com.group1.aeropace.dto.admin.response.LowStockResponse;
import com.group1.aeropace.dto.admin.response.NewCustomerResponse;
import com.group1.aeropace.dto.admin.response.TopBuyerResponse;
import com.group1.aeropace.entity.CustomerProfile;
import com.group1.aeropace.entity.User;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.repository.CustomerProfileRepository;
import com.group1.aeropace.repository.OrderRepository;
import com.group1.aeropace.repository.ProductVariantRepository;
import com.group1.aeropace.repository.UserRepository;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final InventoryService inventoryService;
    private final OrderRepository orderRepository;

    // ── Low Stock ─────────────────────────────────────────────
    public List<LowStockResponse> getLowStock(int threshold, int limit) {
        return productVariantRepository
                .findLowStockVariants(threshold)
                .stream()
                .limit(limit)
                .map(v -> LowStockResponse.builder()
                        .variantId(v.getId())
                        .productId(v.getProduct().getId())
                        .productName(v.getProduct().getName())
                        .sku(v.getSku())
                        .stock(inventoryService.getAvailableStockByVariantId(v.getId()))
                        .build())
                .toList();
    }

    // ── Top Buyers ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<TopBuyerResponse> getTopBuyers(String dateFrom, String dateTo, int limit) {
        LocalDateTime from = LocalDate.parse(dateFrom).atStartOfDay();
        LocalDateTime to   = LocalDate.parse(dateTo).atTime(23, 59, 59);

        List<Object[]> rows = orderRepository.findTopBuyersBetween(
                from, to, List.of(OrderStatus.DELIVERED, OrderStatus.COMPLETED), PageRequest.of(0, limit));

        if (rows.isEmpty()) return List.of();

        List<Long> userIds = rows.stream().map(r -> (Long) r[0]).toList();
        Map<Long, String> emailMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));
        Map<Long, CustomerProfile> profileMap = customerProfileRepository.findByUser_IdIn(userIds).stream()
                .collect(Collectors.toMap(cp -> cp.getUser().getId(), cp -> cp));

        return rows.stream().map(r -> {
            Long userId = (Long) r[0];
            CustomerProfile profile = profileMap.get(userId);
            return TopBuyerResponse.builder()
                    .userId(userId)
                    .fullName(profile != null ? profile.getFullName() : "Unknown")
                    .email(emailMap.getOrDefault(userId, "—"))
                    .orderCount((Long) r[1])
                    .totalSpent((BigDecimal) r[2])
                    .build();
        }).toList();
    }

    // ── New Customers ─────────────────────────────────────────
    public List<NewCustomerResponse> getNewCustomers(String dateFrom, String dateTo) {
        LocalDateTime from = LocalDate.parse(dateFrom).atStartOfDay();
        LocalDateTime to   = LocalDate.parse(dateTo).atTime(23, 59, 59);
        List<User> users = userRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to);
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, CustomerProfile> profileMap = customerProfileRepository.findByUser_IdIn(userIds)
                .stream().collect(Collectors.toMap(cp -> cp.getUser().getId(), cp -> cp));
        return users.stream()
                .map(u -> {
                    CustomerProfile profile = profileMap.get(u.getId());
                    if (profile == null) throw new AppException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND);
                    return NewCustomerResponse.builder()
                            .userId(u.getId())
                            .fullName(profile.getFullName())
                            .email(u.getEmail())
                            .createdAt(u.getCreatedAt())
                            .build();
                })
                .toList();
    }
}

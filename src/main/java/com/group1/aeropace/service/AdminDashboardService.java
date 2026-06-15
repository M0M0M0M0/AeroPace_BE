package com.group1.aeropace.service;

import com.group1.aeropace.dto.admin.response.LowStockResponse;
import com.group1.aeropace.dto.admin.response.NewCustomerResponse;
import com.group1.aeropace.entity.CustomerProfile;
import com.group1.aeropace.entity.User;
import com.group1.aeropace.repository.CustomerProfileRepository;
import com.group1.aeropace.repository.ProductVariantRepository;
import com.group1.aeropace.repository.UserRepository;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    // ── Low Stock ─────────────────────────────────────────────
    public List<LowStockResponse> getLowStock(int threshold, int limit) {
        return productVariantRepository
                .findByStockLessThanEqualAndIsDeletedFalseOrderByStockAsc(threshold)
                .stream()
                .limit(limit)
                .map(v -> LowStockResponse.builder()
                        .variantId(v.getId())
                        .productName(v.getProduct().getName())
                        .sku(v.getSku())
                        .stock(v.getStock())
                        .build())
                .toList();
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

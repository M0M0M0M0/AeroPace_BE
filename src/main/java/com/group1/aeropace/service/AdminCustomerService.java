package com.group1.aeropace.service;

import com.group1.aeropace.dto.admin.response.AdminCustomerDetailResponse;
import com.group1.aeropace.dto.admin.response.AdminCustomerResponse;
import com.group1.aeropace.entity.CustomerProfile;
import com.group1.aeropace.entity.User;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.repository.CustomerProfileRepository;
import com.group1.aeropace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminCustomerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    public List<AdminCustomerResponse> getAllCustomers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != null &&
                        u.getRole().getName().equalsIgnoreCase("USER"))
                .map(this::mapToResponse)
                .toList();
    }

    // Dùng cho trang Admin Customers (pagination) — cùng bộ filter với UI hiện tại, áp dụng và cắt trang trong bộ nhớ
    // (User/CustomerProfile không có Specification sẵn, giữ nguyên cách map hiện có thay vì viết query join mới).
    public Map<String, Object> getAllCustomersPaged(
            String searchId, String searchName, String searchEmail, String searchPhone,
            String status, String dateFrom, String dateTo,
            int page, int size
    ) {
        List<AdminCustomerResponse> filtered = getAllCustomers().stream()
                .filter(c -> searchId == null || searchId.isBlank()
                        || String.valueOf(c.getUserId()).contains(searchId.trim()))
                .filter(c -> searchName == null || searchName.isBlank()
                        || ((c.getFullName() != null ? c.getFullName() : "") + " " + (c.getUsername() != null ? c.getUsername() : ""))
                                .toLowerCase().contains(searchName.toLowerCase()))
                .filter(c -> searchEmail == null || searchEmail.isBlank()
                        || (c.getEmail() != null && c.getEmail().toLowerCase().contains(searchEmail.toLowerCase())))
                .filter(c -> searchPhone == null || searchPhone.isBlank()
                        || (c.getPhoneNumber() != null && c.getPhoneNumber().contains(searchPhone.trim())))
                .filter(c -> status == null || status.isBlank() || status.equals("ALL")
                        || status.equalsIgnoreCase(c.getStatus()))
                .filter(c -> {
                    if ((dateFrom == null || dateFrom.isBlank()) && (dateTo == null || dateTo.isBlank())) return true;
                    if (c.getCreatedAt() == null) return false;
                    LocalDate d = c.getCreatedAt().toLocalDate();
                    if (dateFrom != null && !dateFrom.isBlank() && d.isBefore(LocalDate.parse(dateFrom))) return false;
                    if (dateTo != null && !dateTo.isBlank() && d.isAfter(LocalDate.parse(dateTo))) return false;
                    return true;
                })
                .toList();

        int totalElements = filtered.size();
        int totalPages = Math.max((int) Math.ceil(totalElements / (double) size), 1);
        int from = Math.min(page * size, totalElements);
        int to = Math.min(from + size, totalElements);

        return Map.of(
                "customers", filtered.subList(from, to),
                "totalPages", totalPages,
                "totalElements", totalElements
        );
    }

    public AdminCustomerDetailResponse getCustomerDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CustomerProfile profile = customerProfileRepository
                .findByUser_Id(userId)
                .orElse(null);

        return AdminCustomerDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .fullName(profile != null ? profile.getFullName() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .gender(profile != null ? profile.getGender() : null)
                .dob(profile != null ? profile.getDob() : null)
                .address(profile != null ? profile.getAddress() : null)
                .ward(profile != null ? profile.getWard() : null)
                .district(profile != null ? profile.getDistrict() : null)
                .province(profile != null ? profile.getProvince() : null)
                .profileCreatedAt(profile != null ? profile.getCreatedAt() : null)
                .profileUpdatedAt(profile != null ? profile.getUpdatedAt() : null)
                .build();
    }

    public AdminCustomerResponse toggleLockCustomer(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newStatus = "ACTIVE".equalsIgnoreCase(user.getStatus()) ? "LOCKED" : "ACTIVE";
        user.setStatus(newStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return mapToResponse(user);
    }

    private AdminCustomerResponse mapToResponse(User user) {
        CustomerProfile profile = customerProfileRepository
                .findByUser_Id(user.getId())
                .orElse(null);

        return new AdminCustomerResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt(),
                profile != null ? profile.getId()          : null,
                profile != null ? profile.getFullName()    : "",
                profile != null ? profile.getPhoneNumber() : "",
                profile != null ? profile.getAddress()     : "",
                profile != null ? profile.getGender()      : "",
                profile != null ? profile.getDob()         : null
        );
    }
}

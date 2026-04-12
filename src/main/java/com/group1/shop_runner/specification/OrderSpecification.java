package com.group1.shop_runner.specification;

import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class OrderSpecification {

    public static Specification<Order> build(
            String id, String receiverName, String phoneNumber,
            String shippingAddress, String status,
            String dateFrom, String dateTo
    ) {
        return Specification
                .where(likeId(id))
                .and(likeReceiverName(receiverName))
                .and(likePhoneNumber(phoneNumber))
                .and(likeAddress(shippingAddress))
                .and(equalStatus(status))
                .and(fromDate(dateFrom))
                .and(toDate(dateTo));
    }

    private static Specification<Order> likeId(String id) {
        return (root, query, cb) -> {
            if (id == null || id.isBlank()) return null;
            return cb.like(root.get("id").as(String.class), "%" + id.trim() + "%");
        };
    }

    private static Specification<Order> likeReceiverName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("receiverName")), "%" + name.toLowerCase() + "%");
        };
    }

    private static Specification<Order> likePhoneNumber(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.isBlank()) return null;
            return cb.like(root.get("phoneNumber"), "%" + phone.trim() + "%");
        };
    }

    private static Specification<Order> likeAddress(String address) {
        return (root, query, cb) -> {
            if (address == null || address.isBlank()) return null;
            return cb.like(cb.lower(root.get("shippingAddress")), "%" + address.toLowerCase() + "%");
        };
    }

    private static Specification<Order> equalStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank() || status.equals("ALL")) return null;
            return cb.equal(root.get("status"), OrderStatus.valueOf(status));
        };
    }

    private static Specification<Order> fromDate(String dateFrom) {
        return (root, query, cb) -> {
            if (dateFrom == null || dateFrom.isBlank()) return null;
            return cb.greaterThanOrEqualTo(root.get("createdAt"),
                    LocalDate.parse(dateFrom).atStartOfDay());
        };
    }

    private static Specification<Order> toDate(String dateTo) {
        return (root, query, cb) -> {
            if (dateTo == null || dateTo.isBlank()) return null;
            return cb.lessThanOrEqualTo(root.get("createdAt"),
                    LocalDate.parse(dateTo).atTime(23, 59, 59));
        };
    }
}

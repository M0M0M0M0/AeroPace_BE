package com.group1.aeropace.specification;

import com.group1.aeropace.entity.Order;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.enums.PaymentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

public class OrderSpecification {

    public static Specification<Order> build(
            String orderCode, String receiverName, String phoneNumber,
            String shippingAddress, String status,
            String dateFrom, String dateTo, Long userId
    ) {
        return Specification
                .where(likeOrderCode(orderCode))
                .and(likeReceiverName(receiverName))
                .and(likePhoneNumber(phoneNumber))
                .and(likeAddress(shippingAddress))
                .and(matchStatus(status))
                .and(fromDate(dateFrom))
                .and(toDate(dateTo))
                .and(equalUserId(userId))
                .and(notFailedPayment());
    }

    private static Specification<Order> likeOrderCode(String orderCode) {
        return (root, query, cb) -> {
            if (orderCode == null || orderCode.isBlank()) return null;
            return cb.like(root.get("orderCode"), "%" + orderCode.trim() + "%");
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

    // "status" chấp nhận cả các nhóm ảo dùng riêng cho admin order list (không phải giá trị OrderStatus thật):
    // PENDING_ACTION = đơn PAID cần xử lý; CANCELLED = đã hủy hoặc đang chờ hoàn tiền (trừ đã hoàn xong); REFUNDED = đã hoàn tiền.
    private static Specification<Order> matchStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank() || status.equals("ALL")) return null;
            switch (status) {
                case "PENDING_ACTION":
                    return cb.equal(root.get("status"), OrderStatus.PAID);
                case "REFUNDED":
                    return cb.equal(root.get("paymentStatus"), PaymentStatus.REFUNDED);
                case "CANCELLED": {
                    Predicate cancelledOrRefundPending = cb.or(
                            cb.equal(root.get("status"), OrderStatus.CANCELLED),
                            cb.equal(root.get("paymentStatus"), PaymentStatus.REFUND_PENDING)
                    );
                    Predicate notRefunded = cb.notEqual(root.get("paymentStatus"), PaymentStatus.REFUNDED);
                    return cb.and(cancelledOrRefundPending, notRefunded);
                }
                default:
                    return cb.equal(root.get("status"), OrderStatus.valueOf(status));
            }
        };
    }

    private static Specification<Order> notFailedPayment() {
        return (root, query, cb) -> cb.notEqual(root.get("paymentStatus"), PaymentStatus.FAILED);
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

    private static Specification<Order> equalUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            return cb.equal(root.get("user").get("id"), userId);
        };
    }
}

package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.order.request.CheckoutRequest;
import com.group1.shop_runner.dto.order.response.OrderDetailResponse;
import com.group1.shop_runner.dto.order.response.OrderItemResponse;
import com.group1.shop_runner.dto.order.response.OrderListResponse;
import com.group1.shop_runner.entity.*;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.repository.*;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.specification.OrderSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Tạo đơn hàng từ toàn bộ cart của user.
     * Flow: validate input → tạo Order rỗng → duyệt cart → trừ stock → lưu OrderItem → xóa cart.
     * <p>
     * Order được save trước khi duyệt cart để có orderId làm FK cho OrderItem.
     * totalPrice được tính lại từ đầu thay vì tin vào client để tránh giả mạo giá.
     * <p>
     * Side effects: trừ stock variant, xóa toàn bộ cart của user sau khi thành công.
     * Toàn bộ wrapped trong @Transactional — rollback nếu bất kỳ bước nào thất bại.
     *
     * @throws AppException CART_IS_EMPTY, VARIANT_NOT_FOUND, PRODUCT_NOT_FOUND, EXCEED_STOCK
     */
    @Transactional
    public Order checkout(CheckoutRequest request) {

        if (request.getUserId() == null ||
                request.getShippingAddress() == null || request.getShippingAddress().isBlank() ||
                request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(request.getUserId());

        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }

        Order order = new Order();

        User user = new User();
        user.setId(request.getUserId());

        order.setUser(user);
        order.setNote(request.getNote());
        String paymentMethod = request.getPaymentMethod();
        // Status phân biệt theo phương thức thanh toán — COD chưa thu tiền, các method khác coi là đã thanh toán
        if ("cod".equalsIgnoreCase(paymentMethod)) {
            order.setStatus(OrderStatus.SHIP_COD);
        } else {
            order.setStatus(OrderStatus.PAID);
        }
        order.setShippingAddress(request.getShippingAddress());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setReceiverName(request.getReceiverName());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setTotalPrice(BigDecimal.ZERO);

        // Save trước để có orderId cho OrderItem FK
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();

            // Re-fetch từ DB để đảm bảo variant/product chưa bị xóa sau khi user thêm vào cart
            if (variant == null) {
                throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            }
            variant = productVariantRepository.findById(variant.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

            Product product = variant.getProduct();
            if (product == null || product.getStatus() == Product.Status.DELETED) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            if (variant.getIsDeleted() == true) {
                throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            }

            Integer quantity = cartItem.getQuantity();
            BigDecimal price = variant.getPrice();

            if (quantity == null || quantity <= 0 || price == null) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            total = total.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(price);

            // Snapshot tên/variant tại thời điểm đặt hàng — tránh bị ảnh hưởng nếu product thay đổi sau này
            orderItem.setProductName(variant.getProduct().getName());
            orderItem.setVariantName(buildVariantName(variant));
            orderItem.setSku(variant.getSku() != null ? variant.getSku() : "");
            orderItem.setProductImgUrl(null);
            orderItem.setNote(null);

            if (variant.getStock() >= quantity) {
                variant.setStock(variant.getStock() - quantity);
            } else {
                throw new AppException(ErrorCode.EXCEED_STOCK);
            }

            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);
        List<ProductVariant> updatedVariants = orderItems.stream()
                .map(OrderItem::getProductVariant)
                .toList();
        productVariantRepository.saveAll(updatedVariants);

        order.setTotalPrice(total);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        cartItemRepository.deleteByUserId(request.getUserId());

        return order;
    }

    /**
     * Lấy lịch sử đơn hàng của một user, kèm danh sách item trong mỗi đơn.
     * Mỗi order trigger thêm một query lấy OrderItem — chấp nhận được với lịch sử cá nhân,
     * nhưng không nên dùng pattern này cho admin list nhiều user.
     *
     * @throws AppException INVALID_INPUT nếu userId null
     */
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrdersByUserId(Long userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(this::mapToOrderListResponse)
                .toList();
    }

    /**
     * Lấy chi tiết một đơn hàng kèm toàn bộ OrderItem.
     * Dữ liệu item là snapshot — phản ánh tên/giá tại thời điểm đặt, không thay đổi theo product hiện tại.
     *
     * @throws AppException ORDER_NOT_FOUND
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(Integer orderId) {
        if (orderId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        return mapToOrderDetailResponse(order, orderItems);
    }

    /**
     * Cập nhật trạng thái đơn hàng với phân quyền theo role:
     * - Admin: được phép thực hiện mọi transition hợp lệ.
     * - User thường: chỉ được CANCEL đơn của chính mình, và chỉ khi đơn chưa SHIPPING/DELIVERED/CANCELLED.
     * <p>
     * Side effect khi CANCEL: hoàn trả stock về tất cả variant trong đơn.
     *
     * @param authentication thông tin người dùng hiện tại để xác định quyền và ownership
     * @throws AppException ORDER_NOT_FOUND, INVALID_ORDER_STATUS_UPDATE, ORDER_ACCESS_DENIED, INVALID_STATUS_TRANSITION
     */
    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatus newStatus, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
        String currentUserEmail = currentUser.getEmail();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // User thường chỉ được cancel, và không được cancel đơn đang/đã xử lý
        if (!isAdmin &&
                (newStatus != OrderStatus.CANCELLED
                        || order.getStatus() == OrderStatus.SHIPPING
                        || order.getStatus() == OrderStatus.DELIVERED
                        || order.getStatus() == OrderStatus.CANCELLED)
        ) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_UPDATE);
        }

        if (isAdmin || order.getUser().getEmail().equals(currentUserEmail)) {
            validateStatusTransition(order.getStatus(), newStatus);

            order.setStatus(newStatus);
            order.setUpdatedAt(LocalDateTime.now());

            // Hoàn trả stock khi hủy đơn
            if (newStatus == OrderStatus.CANCELLED) {
                List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
                for (OrderItem item : items) {
                    ProductVariant variant = item.getProductVariant();
                    variant.setStock(variant.getStock() + item.getQuantity());
                    productVariantRepository.save(variant);
                }
            }

            orderRepository.save(order);
        } else {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    /**
     * Kiểm tra transition status có hợp lệ không theo state machine:
     * PAID/SHIP_COD → SHIPPING hoặc CANCELLED
     * SHIPPING      → DELIVERED
     * DELIVERED/CANCELLED → không cho phép bất kỳ transition nào
     *
     * @throws AppException INVALID_STATUS_TRANSITION nếu transition không hợp lệ
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PAID, SHIP_COD -> next == OrderStatus.SHIPPING || next == OrderStatus.CANCELLED;
            case SHIPPING        -> next == OrderStatus.DELIVERED;
            case DELIVERED,
                 CANCELLED       -> false;
        };

        if (!valid) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    /**
     * Hủy đơn hàng — shortcut không cần authentication, dùng cho luồng user tự hủy từ client.
     * Không hoàn trả stock ở đây — nếu cần hoàn stock khi user hủy, dùng {@code updateOrderStatus}.
     *
     * @throws AppException ORDER_NOT_FOUND, INVALID_STATUS_TRANSITION
     */
    @Transactional
    public void cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    /**
     * Map Order sang list response, load thêm OrderItem trong cùng call.
     * Chú ý: mỗi lần gọi mapper này trigger một query riêng lấy items — cân nhắc nếu list lớn.
     */
    private OrderListResponse mapToOrderListResponse(Order order) {
        OrderListResponse response = new OrderListResponse();
        response.setId(order.getId());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setReceiverName(order.getReceiverName());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductVariantId(Math.toIntExact(item.getProductVariant().getId()));
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setVariantName(item.getVariantName());
            itemResponse.setSku(item.getSku());
            itemResponse.setProductImgUrl(item.getProductImgUrl());
            itemResponse.setNote(item.getNote());
            return itemResponse;
        }).toList();

        response.setItems(itemResponses);
        return response;
    }

    private OrderDetailResponse mapToOrderDetailResponse(Order order, List<OrderItem> orderItems) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setReceiverName(order.getReceiverName());
        response.setNote(order.getNote());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = orderItems.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductVariantId(Math.toIntExact(item.getProductVariant().getId()));
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            // Đọc từ snapshot — không lazy-load product để tránh thay đổi sau đặt hàng ảnh hưởng response
            itemResponse.setProductName(item.getProductName());
            itemResponse.setVariantName(item.getVariantName());
            itemResponse.setSku(item.getSku());
            itemResponse.setProductImgUrl(item.getProductImgUrl());
            itemResponse.setNote(item.getNote());
            return itemResponse;
        }).toList();

        response.setItems(items);
        return response;
    }

    /**
     * Lấy toàn bộ đơn hàng cho admin với filter động qua Specification.
     * Tất cả tham số filter đều optional — null đồng nghĩa không lọc theo field đó.
     * Kết quả sắp xếp theo id tăng dần.
     */
    public List<OrderListResponse> getAllOrders(
            String id, String receiverName, String phoneNumber,
            String shippingAddress, String status,
            String dateFrom, String dateTo
    ) {
        var spec = OrderSpecification.build(
                id, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo
        );
        return orderRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::mapToOrderListResponse)
                .toList();
    }

    /**
     * Ghép các option value của variant thành chuỗi hiển thị, bỏ qua option null hoặc blank.
     * Ví dụ: "Đỏ / XL" hoặc "Xanh" nếu chỉ có một option.
     */
    private String buildVariantName(ProductVariant variant) {
        List<String> parts = new ArrayList<>();
        if (variant.getOption1Value() != null && !variant.getOption1Value().isBlank())
            parts.add(variant.getOption1Value());
        if (variant.getOption2Value() != null && !variant.getOption2Value().isBlank())
            parts.add(variant.getOption2Value());
        if (variant.getOption3Value() != null && !variant.getOption3Value().isBlank())
            parts.add(variant.getOption3Value());
        return String.join(" / ", parts);
    }
}
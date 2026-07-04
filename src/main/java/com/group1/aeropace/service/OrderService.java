package com.group1.aeropace.service;

import com.group1.aeropace.dto.order.request.CheckoutRequest;
import com.group1.aeropace.dto.order.request.UpdatePaymentRequest;
import com.group1.aeropace.dto.order.response.OrderDetailResponse;
import com.group1.aeropace.dto.order.response.OrderItemResponse;
import com.group1.aeropace.dto.order.response.OrderListResponse;
import com.group1.aeropace.entity.*;
import com.group1.aeropace.enums.CancelType;
import com.group1.aeropace.enums.OrderStatus;
import com.group1.aeropace.enums.PaymentMethod;
import com.group1.aeropace.enums.PaymentStatus;
import com.group1.aeropace.event.OrderConfirmedEvent;
import com.group1.aeropace.repository.*;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import com.group1.aeropace.specification.OrderSpecification;
import com.stripe.exception.StripeException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Autowired
    private ShippingMethodRepository shippingMethodRepository;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ReviewService reviewService;

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
        // Tự động hủy đơn PENDING trước đó trước khi bắt đầu luồng thanh toán mới
        orderRepository.findByUserId(request.getUserId())
                .stream()
                .filter(order -> OrderStatus.PENDING.equals(order.getStatus())
                        )
                .forEach(order -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setPaymentStatus(PaymentStatus.FAILED);
                    order.setCancelType(CancelType.PAYMENT_REPLACED);
                    order.setCancelReason("Người dùng khởi tạo thanh toán mới");
                    orderRepository.save(order);
                });
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
        if ("paypal".equalsIgnoreCase(paymentMethod)) {
            order.setPaymentMethod(PaymentMethod.PAYPAL);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            order.setPaymentMethod(PaymentMethod.STRIPE);
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        order.setShippingAddress(request.getShippingAddress());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setReceiverName(request.getReceiverName());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setTotalPrice(BigDecimal.ZERO);
        order.setWard(request.getWard());
        order.setDistrict(request.getDistrict());
        order.setProvince(request.getProvince());
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_METHOD_NOT_FOUND));
        order.setShippingMethod(shippingMethod.getName());
        order.setShippingFee(shippingMethod.getFee());
        order.setPaymentOrderId(request.getPaymentOrderId());
        order.setOrderCode(generateOrderCode());
        // Save trước để có orderId cho OrderItem FK
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();

            // Refetch để đảm bảo variant chưa bị xóa sau khi thêm vào cart
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
            orderItem.setVariantName(buildVariantName(variant));
            orderItem.setQuantity(quantity);
            orderItem.setPrice(price);

            orderItem.setProductName(variant.getProduct().getName());
            orderItem.setSku(variant.getSku() != null ? variant.getSku() : "");
            orderItem.setProductImgUrl(variant.getProduct().getImages()
                    .stream()
                    .filter(img -> img.getPosition() == 1)
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(null));
            orderItem.setBrandName(variant.getProduct().getBrand() != null
                    ? variant.getProduct().getBrand().getName() : null);
            orderItem.setProductId(variant.getProduct().getId());
            orderItem.setNote(null);

            inventoryService.deductStock(variant.getId(), quantity, order.getId());

            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);

        BigDecimal vat = total.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        order.setVat(vat);
        order.setTotalPrice(total.add(vat).add(order.getShippingFee()));
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);



        return order;
    }
    /**
     * Cập nhật trạng thái thanh toán của đơn hàng sau khi thanh toán thành công.
     * Xóa cart của user và publish OrderConfirmedEvent để gửi email xác nhận bất đồng bộ.
     */
    @Transactional
    public void updatePayment(Long orderId, UpdatePaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        order.setPaymentOrderId(request.getPaymentOrderId());
        if (order.getPaymentMethod() == PaymentMethod.STRIPE
                && request.getPaymentOrderId() != null) {
            try {
                String chargeId = stripeService.getChargeId(request.getPaymentOrderId());
                order.setPaymentTransactionId(chargeId);
            } catch (StripeException e) {
                log.warn("Không thể lấy chargeId từ Stripe: {}", e.getMessage());
            }
        } else {
            order.setPaymentTransactionId(request.getPaymentTransactionId());
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.PAID);
        if ("PAID".equals(request.getPaymentStatus())) {
            order.setStatus(OrderStatus.PAID);
        }
        order.setUpdatedAt(LocalDateTime.now());

        cartItemRepository.deleteByUserId(order.getUser().getId());
        orderRepository.save(order);

        eventPublisher.publishEvent(new OrderConfirmedEvent(this, order));
    }
    /**
     * Lấy lịch sử đơn hàng của một user, kèm danh sách item trong mỗi đơn.
     * @throws AppException INVALID_INPUT nếu userId null
     */
    @Transactional(readOnly = true)
    public List<OrderListResponse> getOrdersByUserId(Long userId, Authentication authentication) {
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = userId.equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        List<Order> orders = orderRepository.findByUserId(userId)
                .stream().filter( order -> order.getStatus() != OrderStatus.PENDING)
                .toList();


        return orders.stream()
                .filter(order-> order.getCancelType() != CancelType.PAYMENT_REPLACED
                && order.getCancelType() != CancelType.PAYMENT_TIMEOUT)
                .map(this::mapToOrderListResponse)
                .toList();
    }

    /**
     * Cập nhật trạng thái đơn hàng với phân quyền theo role:
     * - Admin: được phép thực hiện mọi transition hợp lệ.
     * <p>
     * Side effect khi CANCEL: hoàn trả stock về tất cả variant trong đơn.
     *
     * @throws AppException ORDER_NOT_FOUND, INVALID_ORDER_STATUS_UPDATE, ORDER_ACCESS_DENIED, INVALID_STATUS_TRANSITION
     */
    @Transactional
    public void updateOrderStatus(String orderCode, OrderStatus newStatus, String reason) {

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        if(order.getPaymentStatus() == PaymentStatus.PAID && newStatus == OrderStatus.CANCELLED){
            order.setPaymentStatus(PaymentStatus.REFUND_PENDING) ;
        }
        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancelType(CancelType.ADMIN_CANCELLED);
            order.setCancelReason(reason);
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                inventoryService.restoreStock(item.getProductVariant().getId(), item.getQuantity(), order.getId());
            }
        }

        orderRepository.save(order);
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

            case PENDING ->
                    next == OrderStatus.PAID
                            || next == OrderStatus.CANCELLED;

            case PAID ->
                    next == OrderStatus.SHIPPING
                            || next == OrderStatus.CANCELLED;

            case SHIPPING ->
                    next == OrderStatus.DELIVERED;

            case COMPLETED,
                 CANCELLED,
                 DELIVERED->
                    false;
        };

        if (!valid) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    /**
     * Hủy đơn hàng — cần authentication, dùng cho luồng user tự hủy từ client.
     *
     * @throws AppException ORDER_NOT_FOUND, INVALID_STATUS_TRANSITION
     */
    @Transactional
    public void cancelOrder(String orderCode, Authentication authentication, String cancelNote) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!order.getUser().getEmail().equals(currentUser.getEmail())) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() == OrderStatus.SHIPPING
                || order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelType(CancelType.USER_CANCELLED);
        order.setCancelReason(cancelNote);
        if(order.getPaymentStatus() == PaymentStatus.PAID){
            order.setPaymentStatus(PaymentStatus.REFUND_PENDING) ;
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            inventoryService.restoreStock(item.getProductVariant().getId(), item.getQuantity(), order.getId());
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * Map Order sang OrderListResponse, load thêm OrderItem trong cùng l���n gọi.
     * Lưu ý: mỗi lần gọi mapper này trigger một query riêng để lấy items — cân nhắc nếu danh sách lớn.
     */
    private OrderListResponse mapToOrderListResponse(Order order) {
        OrderListResponse response = new OrderListResponse();
        response.setOrderCode(order.getOrderCode());
        response.setTotalPrice(order.getTotalPrice());
        response.setShippingFee(order.getShippingFee());
        response.setVat(order.getVat());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setWard(order.getWard());
        response.setDistrict(order.getDistrict());
        response.setProvince(order.getProvince());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setReceiverName(order.getReceiverName());
        response.setCreatedAt(order.getCreatedAt());
        response.setCancelType(order.getCancelType());
        response.setCancelNote(order.getCancelReason());
        response.setPaymentStatus(order.getPaymentStatus());

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductId(item.getProductId());
            itemResponse.setProductVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null);
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setVariantName(item.getVariantName());
            itemResponse.setSku(item.getSku());
            itemResponse.setProductImgUrl(item.getProductImgUrl());
            itemResponse.setBrandName(item.getBrandName());
            itemResponse.setNote(item.getNote());
            return itemResponse;
        }).toList();

        response.setItems(itemResponses);
        return response;
    }

    private OrderDetailResponse mapToOrderDetailResponse(Order order, List<OrderItem> orderItems) {
        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderCode(order.getOrderCode());
        response.setUsername(order.getUser().getUsername());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setWard(order.getWard());
        response.setDistrict(order.getDistrict());
        response.setProvince(order.getProvince());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setReceiverName(order.getReceiverName());
        response.setNote(order.getNote());
        response.setCreatedAt(order.getCreatedAt());

        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setShippingFee(order.getShippingFee());
        response.setVat(order.getVat());
        response.setCancelReason(order.getCancelReason());
        response.setCancelType(order.getCancelType());


        List<OrderItemResponse> items = orderItems.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductId(item.getProductId());
            itemResponse.setProductVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null);
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setVariantName(item.getVariantName());
            itemResponse.setSku(item.getSku());
            itemResponse.setProductImgUrl(item.getProductImgUrl());
            itemResponse.setBrandName(item.getBrandName());
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
            String orderCode, String receiverName, String phoneNumber,
            String shippingAddress, String status,
            String dateFrom, String dateTo, Long userId
    ) {
        var spec = OrderSpecification.build(
                orderCode, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo, userId
        );
        return orderRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::mapToOrderListResponse)
                .toList();
    }

    // Dùng cho trang Admin Orders (server-side pagination) — cùng bộ filter với getAllOrders nhưng phân trang tại DB.
    public Map<String, Object> getAllOrdersPaged(
            String orderCode, String receiverName, String phoneNumber,
            String shippingAddress, String status,
            String dateFrom, String dateTo, Long userId,
            int page, int size
    ) {
        var spec = OrderSpecification.build(
                orderCode, receiverName, phoneNumber, shippingAddress, status, dateFrom, dateTo, userId
        );
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> result = orderRepository.findAll(spec, pageable);
        List<OrderListResponse> orders = result.getContent().stream()
                .map(this::mapToOrderListResponse)
                .toList();
        return Map.of(
                "orders", orders,
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements()
        );
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

    // Lấy chi tiết đơn hàng cho admin (bao gồm đầy đủ thông tin thanh toán và refund)
    public OrderDetailResponse getOrderDetail(String orderCode){
        OrderDetailResponse response = new OrderDetailResponse();
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_FOUND));
        response.setOrderCode(order.getOrderCode());
        response.setUserId(order.getUser().getId());
        response.setUsername(order.getUser().getUsername());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setWard(order.getWard());
        response.setDistrict(order.getDistrict());
        response.setProvince(order.getProvince());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setReceiverName(order.getReceiverName());
        response.setNote(order.getNote());
        response.setCreatedAt(order.getCreatedAt());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setShippingFee(order.getShippingFee());
        response.setVat(order.getVat());
        response.setCancelReason(order.getCancelReason());
        response.setCancelType(order.getCancelType());
        response.setPaymentOrderId(order.getPaymentOrderId());
        response.setPaymentTransactionId(order.getPaymentTransactionId());
        response.setRefundReason(order.getRefundReason());

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = orderItems.stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductId(item.getProductId());
            itemResponse.setProductVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null);
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());
            itemResponse.setProductName(item.getProductName());
            itemResponse.setVariantName(item.getVariantName());
            itemResponse.setSku(item.getSku());
            itemResponse.setProductImgUrl(item.getProductImgUrl());
            itemResponse.setBrandName(item.getBrandName());
            itemResponse.setNote(item.getNote());
            return itemResponse;
        }).toList();
        response.setItems(itemResponses);

        return response;
    }

    /**
     * Sinh mã đơn hàng theo định dạng ORD-xddyyxxmmyxxxy-XXXXX.
     * Ngày đặt hàng được nhúng vào đoạn giữa theo template (x=random, d=day, m=month, y=year):
     *   pos  1    → x  (random)
     *   pos  2-3  → dd (ngày)
     *   pos  4-5  → y₀y₁ (năm chữ 1-2, e.g. "20" của 2026)
     *   pos  6-7  → xx (random)
     *   pos  8-9  → mm (tháng)
     *   pos  10   → y₂ (năm chữ 3, e.g. "2" của 2026)
     *   pos  11-13→ xxx (random)
     *   pos  14   → y₃ (năm chữ 4, e.g. "6" của 2026)
     */
    private String generateOrderCode() {
        LocalDateTime now = LocalDateTime.now();
        String year  = String.format("%04d", now.getYear());
        String month = String.format("%02d", now.getMonthValue());
        String day   = String.format("%02d", now.getDayOfMonth());
        String rnd   = RandomStringUtils.randomAlphanumeric(6).toLowerCase();
        String middle = "" + rnd.charAt(0)
                + day.charAt(0)   + day.charAt(1)
                + year.charAt(0)  + year.charAt(1)
                + rnd.charAt(1)   + rnd.charAt(2)
                + month.charAt(0) + month.charAt(1)
                + year.charAt(2)
                + rnd.charAt(3)   + rnd.charAt(4) + rnd.charAt(5)
                + year.charAt(3);
        String suffix = RandomStringUtils.randomAlphanumeric(5).toUpperCase();
        return "ORD-" + middle + "-" + suffix;
    }
    /**
     * User xác nhận đã nhận hàng — chuyển trạng thái DELIVERED → COMPLETED và tạo slot review PENDING.
     */
    @Transactional
    public void confirmDelivered(String orderCode, Authentication authentication) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!order.getUser().getEmail().equals(currentUser.getEmail())) {
            throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        completeOrder(order);
    }

    /**
     * Chuyển đơn DELIVERED → COMPLETED và tạo slot review PENDING cho từng sản phẩm.
     * Dùng chung bởi confirmDelivered() (user tự xác nhận) và OrderScheduler
     * (tự động hoàn tất sau 1 ngày nếu user không xác nhận).
     */
    @Transactional
    public void completeOrder(Order order) {
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        items.stream()
                .collect(java.util.stream.Collectors.toMap(
                        item -> item.getProductVariant().getProduct().getId(),
                        item -> item,
                        (existing, duplicate) -> existing
                ))
                .values()
                .forEach(item -> {
                    ProductVariant variant = item.getProductVariant();
                    Product product = variant.getProduct();
                    reviewService.createPendingReview(order, product, variant, order.getUser());
                });
    }

}
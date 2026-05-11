package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.order.request.CheckoutRequest;
import com.group1.shop_runner.dto.order.response.OrderDetailResponse;
import com.group1.shop_runner.dto.order.response.OrderItemResponse;
import com.group1.shop_runner.dto.order.response.OrderListResponse;
import com.group1.shop_runner.entity.CartItem;
import com.group1.shop_runner.entity.Order;
import com.group1.shop_runner.entity.OrderItem;
import com.group1.shop_runner.entity.ProductVariant;
import com.group1.shop_runner.entity.User;
import com.group1.shop_runner.enums.OrderStatus;
import com.group1.shop_runner.repository.ProductVariantRepository;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.CartItemRepository;
import com.group1.shop_runner.repository.OrderItemRepository;
import com.group1.shop_runner.repository.OrderRepository;
import com.group1.shop_runner.specification.OrderSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // =========================================================
    // API 1: Checkout
    // POST /api/orders/checkout
    // =========================================================
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

        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();

            if (variant == null) {
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

            //snapshot
            orderItem.setProductName(variant.getProduct().getName());
            orderItem.setVariantName(buildVariantName(variant));
            orderItem.setSku(variant.getSku() != null ? variant.getSku() : "");
            orderItem.setProductImgUrl(null);
            orderItem.setNote(null);

            if (variant.getStock()>=quantity){
                variant.setStock(variant.getStock() - quantity);
            }else {
                throw new AppException(ErrorCode.EXCEED_STOCK);
            }

            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);
        // Save stock
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

    // =========================================================
    // API 2: Lấy danh sách đơn hàng theo userId
    // GET /api/orders/user/{userId}
    // =========================================================
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

    // =========================================================
    // API 3: Lấy chi tiết 1 đơn hàng
    // GET /api/orders/{orderId}
    // =========================================================
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
    // =========================================================
    // API 4: Update Order Status
    // PUT /api/orders/{orderId}/status
    // =========================================================
    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        //hoan tra inventory
        if (newStatus == OrderStatus.CANCELLED){
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items){
                ProductVariant variant = item.getProductVariant();
                variant.setStock(variant.getStock() +item.getQuantity());
                productVariantRepository.save(variant);
            }
        }

        orderRepository.save(order);
    }
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
    // =========================================================
    // API 5: Cancel Order
    // PUT /api/orders/{orderId}/cancel
    // =========================================================
    @Transactional
    public void cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    // =========================================================
    // Mapper: Order -> OrderListResponse
    // =========================================================
    private OrderListResponse mapToOrderListResponse(Order order) {
        OrderListResponse response = new OrderListResponse();
        response.setId(order.getId());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setReceiverName(order.getReceiverName());
        response.setCreatedAt(order.getCreatedAt());
        //san pham
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

    // =========================================================
    // Mapper: Order + OrderItems -> OrderDetailResponse
    // =========================================================
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
    //lay tat ca order
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
    //helper method
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
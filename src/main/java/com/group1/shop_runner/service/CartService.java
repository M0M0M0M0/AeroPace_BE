package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.cart.request.AddToCartRequest;
import com.group1.shop_runner.dto.cart.request.UpdateCartItemRequest;
import com.group1.shop_runner.dto.cart.response.CartItemResponse;
import com.group1.shop_runner.dto.cart.response.CartResponse;
import com.group1.shop_runner.entity.*;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================
    // GET CART (USER ONLY)
    // =========================
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return mapToCartResponse(user, cartItems);
    }

    // =========================
    // ADD TO CART (USER ONLY)
    // =========================
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProductVariant variant;

        if (request.getProductVariantId() != null) {
            variant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        } else if (request.getProductId() != null) {
            variant = productVariantRepository
                    .findFirstByProductIdAndIsDeletedFalseOrderByIdAsc(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (variant.getStock() <= 0) {
            throw new AppException(ErrorCode.OUT_OF_STOCK);
        }

        CartItem cartItem = cartItemRepository
                .findByUserIdAndProductVariantId(userId, variant.getId())
                .orElse(null);

        int currentQty = (cartItem == null) ? 0 : cartItem.getQuantity();
        int newQty = currentQty + request.getQuantity();

        if (newQty > variant.getStock()) {
            throw new AppException(ErrorCode.EXCEED_STOCK);
        }

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(newQty);
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            cartItem.setQuantity(newQty);
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        cartItemRepository.save(cartItem);

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return mapToCartResponse(user, cartItems);
    }

    // =========================
    // UPDATE ITEM
    // =========================
    @Transactional
    public CartResponse updateCartItemQuantity(Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        ProductVariant variant = cartItem.getProductVariant();
        if (request.getQuantity() > variant.getStock()) {
            throw new AppException(ErrorCode.EXCEED_STOCK);
        }
        cartItem.setQuantity(request.getQuantity());
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);

        List<CartItem> cartItems = cartItemRepository.findByUserId(cartItem.getUser().getId());
        return mapToCartResponse(cartItem.getUser(), cartItems);
    }

    // =========================
    // REMOVE ITEM
    // =========================
    @Transactional
    public CartResponse removeCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        User user = cartItem.getUser();
        cartItemRepository.delete(cartItem);

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        return mapToCartResponse(user, cartItems);
    }

    // =========================
    // CLEAR CART
    // =========================
    @Transactional
    public void clearCart(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        cartItemRepository.deleteByUserId(userId);
    }

    // =========================
    // MERGE CART (localStorage → DB khi login)
    // =========================
    @Transactional
    public CartResponse mergeCart(Long userId, List<AddToCartRequest> guestItems) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        for (AddToCartRequest item : guestItems) {
            ProductVariant variant;

            if (item.getProductVariantId() != null) {
                variant = productVariantRepository.findById(item.getProductVariantId())
                        .orElse(null);
            } else if (item.getProductId() != null) {
                variant = productVariantRepository
                        .findFirstByProductIdAndIsDeletedFalseOrderByIdAsc(item.getProductId())
                        .orElse(null);
            } else {
                continue;
            }

            if (variant == null) continue;

            CartItem existing = cartItemRepository
                    .findByUserIdAndProductVariantId(userId, variant.getId())
                    .orElse(null);

            if (existing != null) {
                int mergedQty = existing.getQuantity() + item.getQuantity();
                existing.setQuantity(Math.min(mergedQty, variant.getStock()));
                existing.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(existing);
            } else {
                CartItem newItem = new CartItem();
                newItem.setUser(user);
                newItem.setProductVariant(variant);
                newItem.setQuantity(Math.min(item.getQuantity(), variant.getStock()));
                newItem.setCreatedAt(LocalDateTime.now());
                newItem.setUpdatedAt(LocalDateTime.now());
                cartItemRepository.save(newItem);
            }
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return mapToCartResponse(user, cartItems);
    }

    // =========================
    // MAPPER
    // =========================
    private CartResponse mapToCartResponse(User user, List<CartItem> cartItems) {
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        int totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                user.getId(),
                totalItems,
                totalAmount,
                itemResponses
        );
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getProductVariant();

        BigDecimal lineTotal = variant.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        String imageUrl = variant.getProduct().getImages()
                .stream()
                .sorted(Comparator.comparingInt(img -> img.getPosition() != null ? img.getPosition() : 0))
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElse(null);

        return new CartItemResponse(
                cartItem.getId(),
                variant.getId(),
                variant.getProduct().getId(),
                variant.getProduct().getName(),
                variant.getOption1Value(),
                variant.getOption2Value(),
                variant.getOption3Value(),
                variant.getPrice(),
                cartItem.getQuantity(),
                variant.getStock(),
                lineTotal,
                imageUrl,
                variant.getStock()
        );
    }
}
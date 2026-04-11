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
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================
    // GET CART (USER / SESSION)
    // =========================
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId, String sessionId) {

        Cart cart = null;

        if (userId != null) {
            cart = cartRepository.findByUserId(userId).orElse(null);
        } else if (sessionId != null) {
            cart = cartRepository.findBySessionId(sessionId).orElse(null);
        }

        if (cart == null) {
            return new CartResponse(null, userId, 0, BigDecimal.ZERO, List.of());
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return mapToCartResponse(cart, cartItems);
    }

    // =========================
    // ADD TO CART
    // =========================
    @Transactional
    public CartResponse addToCart(Long userId, String sessionId, AddToCartRequest request) {

        Cart cart;

        // ===== USER =====
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            cart = cartRepository.findByUserId(userId).orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setCreatedAt(LocalDateTime.now());
                newCart.setUpdatedAt(LocalDateTime.now());
                return cartRepository.save(newCart);
            });
        }
        // ===== GUEST =====
        else {
            if (sessionId == null) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            cart = cartRepository.findBySessionId(sessionId).orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setSessionId(sessionId);
                newCart.setCreatedAt(LocalDateTime.now());
                newCart.setUpdatedAt(LocalDateTime.now());
                return cartRepository.save(newCart);
            });
        }

        ProductVariant variant;

        if (request.getProductVariantId() != null) {
            variant = productVariantRepository.findById(request.getProductVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        } else if (request.getProductId() != null) {
            variant = productVariantRepository
                    .findFirstByProductIdOrderByIdAsc(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        } else {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), variant.getId())
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return mapToCartResponse(cart, cartItems);
    }

    // =========================
    // UPDATE ITEM
    // =========================
    @Transactional
    public CartResponse updateCartItemQuantity(Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        cartItem.setQuantity(request.getQuantity());
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return mapToCartResponse(cart, cartItems);
    }

    // =========================
    // REMOVE ITEM
    // =========================
    @Transactional
    public CartResponse removeCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return mapToCartResponse(cart, cartItems);
    }

    // =========================
    // CLEAR CART BY USER
    // =========================
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cartItemRepository.deleteByCartId(cart.getId());

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    // =========================
    // CLEAR CART BY SESSION
    // =========================
    @Transactional
    public void clearCartBySession(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cartItemRepository.deleteByCartId(cart.getId());

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    // =========================
    // MAPPER
    // =========================
    private CartResponse mapToCartResponse(Cart cart, List<CartItem> cartItems) {

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
                cart.getId(),
                cart.getUser() != null ? cart.getUser().getId() : null,
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
                .sorted(Comparator.comparingInt(img -> img.getPosition()))
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
                lineTotal,
                imageUrl
        );
    }
    //merge cart khi user login
    @Transactional
    public void mergeCart(String sessionId, Long userId) {

        Cart sessionCart = cartRepository.findBySessionId(sessionId).orElse(null);
        Cart userCart = cartRepository.findByUserId(userId).orElse(null);

        if (sessionCart == null) return;

        // ===== CASE 1: USER CHƯA CÓ CART =====
        if (userCart == null) {
            sessionCart.setUser(userRepository.findById(userId).orElseThrow());
            sessionCart.setSessionId(null);
            cartRepository.save(sessionCart);
            return;
        }

        // ===== CASE 2: MERGE =====
        List<CartItem> sessionItems = cartItemRepository.findByCartId(sessionCart.getId());
        List<CartItem> userItems = cartItemRepository.findByCartId(userCart.getId());

        for (CartItem item : sessionItems) {
            CartItem existing = userItems.stream()
                    .filter(i -> i.getProductVariant().getId()
                            .equals(item.getProductVariant().getId()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                cartItemRepository.save(existing);
            } else {
                item.setCart(userCart);
                cartItemRepository.save(item);
            }
        }

        userCart.setUser(userRepository.findById(userId).orElseThrow());
        cartRepository.save(userCart);

        cartRepository.delete(sessionCart);
    }
}
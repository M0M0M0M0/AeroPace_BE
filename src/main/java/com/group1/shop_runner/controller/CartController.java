package com.group1.shop_runner.controller;

import com.group1.shop_runner.config.CustomUserDetails;
import com.group1.shop_runner.dto.cart.request.AddToCartRequest;
import com.group1.shop_runner.dto.cart.request.MergeCartRequest;
import com.group1.shop_runner.dto.cart.request.UpdateCartItemRequest;
import com.group1.shop_runner.dto.cart.response.CartResponse;
import com.group1.shop_runner.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
//@CrossOrigin(origins = "*")

public class CartController {

    @Autowired
    private CartService cartService;

    // =========================================================
    // API 1: GET CART (USER / SESSION)
    // =========================================================
    @GetMapping
    public CartResponse getCart(
            @RequestParam(required = false) String sessionId,
            Authentication authentication
    ) {
        Long userId = null;

        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            userId = user.getId();
        }

        return cartService.getCart(userId, sessionId);
    }

    // =========================================================
    // API 2: ADD TO CART
    // =========================================================
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication
    ) {
        Long userId = null;

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            userId = user.getId();
        }

        String sessionId = request.getSessionId();

        // Nếu chưa login và chưa có sessionId → tạo mới
        if (userId == null && (sessionId == null || sessionId.isEmpty())) {
            sessionId = java.util.UUID.randomUUID().toString();
        }

        CartResponse response = cartService.addToCart(userId, sessionId, request);

        // trả sessionId về cho FE
        return ResponseEntity.ok()
                .header("X-Session-Id", sessionId)
                .body(response);
    }

    // =========================================================
    // API 3: UPDATE CART ITEM
    // =========================================================
    @PutMapping("/items/{cartItemId}")
    public CartResponse updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateCartItemQuantity(cartItemId, request);
    }

    // =========================================================
    // API 4: REMOVE ITEM
    // =========================================================
    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeCartItem(@PathVariable Long cartItemId) {
        return cartService.removeCartItem(cartItemId);
    }

    // =========================================================
    // API 5: CLEAR CART BY USER
    // =========================================================
    @DeleteMapping("/clear/user/{userId}")
    public String clearCartByUser(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return "Clear cart successfully";
    }

    // =========================================================
    // API 6: CLEAR CART BY SESSION
    // =========================================================
    @DeleteMapping("/clear/session")
    public String clearCartBySession(@RequestParam String sessionId) {
        cartService.clearCartBySession(sessionId);
        return "Clear session cart successfully";
    }
    //merge cart khi user login
    @PostMapping("/merge")
    public CartResponse mergeCart(@RequestBody MergeCartRequest request) {
        cartService.mergeCart(request.getSessionId(), request.getUserId());
        return cartService.getCart(request.getUserId(), null);
    }

}
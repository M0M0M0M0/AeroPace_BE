package com.group1.shop_runner.controller;

import com.group1.shop_runner.config.CustomUserDetails;
import com.group1.shop_runner.dto.cart.request.AddToCartRequest;
import com.group1.shop_runner.dto.cart.request.UpdateCartItemRequest;
import com.group1.shop_runner.dto.cart.response.CartResponse;
import com.group1.shop_runner.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // =========================================================
    // API 1: GET CART
    // =========================================================
    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return cartService.getCart(userId);
    }

    // =========================================================
    // API 2: ADD TO CART
    // =========================================================
    @PostMapping("/items")
    public CartResponse addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return cartService.addToCart(userId, request);
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
    // API 5: CLEAR CART
    // =========================================================
    @DeleteMapping("/clear")
    public String clearCart(Authentication authentication) {
        Long userId = extractUserId(authentication);
        cartService.clearCart(userId);
        return "Clear cart successfully";
    }

    // =========================================================
    // API 6: MERGE CART (localStorage → DB khi login)
    // =========================================================
    @PostMapping("/merge")
    public CartResponse mergeCart(
            @RequestBody List<AddToCartRequest> guestItems,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return cartService.mergeCart(userId, guestItems);
    }

    // =========================
    // HELPER
    // =========================
    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails user) {
            return user.getId();
        }
        return null;
    }
}
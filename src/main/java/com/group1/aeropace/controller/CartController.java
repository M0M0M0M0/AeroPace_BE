package com.group1.aeropace.controller;

import com.group1.aeropace.config.CustomUserDetails;
import com.group1.aeropace.dto.cart.request.AddToCartRequest;
import com.group1.aeropace.dto.cart.request.UpdateCartItemRequest;
import com.group1.aeropace.dto.cart.response.CartResponse;
import com.group1.aeropace.service.CartService;
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

    @GetMapping
    public CartResponse getCart(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    public CartResponse addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return cartService.addToCart(userId, request);
    }

    @PutMapping("/items/{cartItemId}")
    public CartResponse updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateCartItemQuantity(cartItemId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeCartItem(@PathVariable Long cartItemId) {
        return cartService.removeCartItem(cartItemId);
    }

    @DeleteMapping("/clear")
    public String clearCart(Authentication authentication) {
        Long userId = extractUserId(authentication);
        cartService.clearCart(userId);
        return "Clear cart successfully";
    }

    @PostMapping("/merge")
    public CartResponse mergeCart(
            @Valid @RequestBody List<AddToCartRequest> guestItems,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        return cartService.mergeCart(userId, guestItems);
    }

    // HELPER
    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails user) {
            return user.getId();
        }
        return null;
    }
}

package com.travelease.cart.controller;

import com.travelease.cart.dto.AddToCartRequest;
import com.travelease.cart.dto.CartResponse;
import com.travelease.cart.service.CartService;
import com.travelease.common.dto.ApiResponse;
import com.travelease.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        CartResponse response = cartService.getCart(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Item added to cart"));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String itemId,
            @RequestParam int quantity) {
        CartResponse response = cartService.updateItemQuantity(principal.getUserId(), itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart updated"));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String itemId) {
        CartResponse response = cartService.removeFromCart(principal.getUserId(), itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from cart"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        CartResponse response = cartService.clearCart(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "Cart cleared"));
    }
}

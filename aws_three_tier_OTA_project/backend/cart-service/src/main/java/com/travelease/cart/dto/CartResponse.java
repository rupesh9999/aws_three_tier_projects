package com.travelease.cart.dto;

import com.travelease.cart.model.Cart;
import com.travelease.cart.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String userId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private int totalItems;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartResponse fromCart(Cart cart) {
        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(cart.getItems().stream()
                        .map(CartItemResponse::fromCartItem)
                        .toList())
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .currency(cart.getCurrency())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CartItemResponse {
    private String itemId;
    private String itemType;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer quantity;
    private BigDecimal subtotal;
    private LocalDateTime travelDate;
    private LocalDateTime returnDate;
    private String imageUrl;
    private String additionalInfo;
    private LocalDateTime addedAt;

    public static CartItemResponse fromCartItem(CartItem item) {
        return CartItemResponse.builder()
                .itemId(item.getItemId())
                .itemType(item.getItemType().name())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .currency(item.getCurrency())
                .quantity(item.getQuantity())
                .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .travelDate(item.getTravelDate())
                .returnDate(item.getReturnDate())
                .imageUrl(item.getImageUrl())
                .additionalInfo(item.getAdditionalInfo())
                .addedAt(item.getAddedAt())
                .build();
    }
}

package com.travelease.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {

    private String userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Builder.Default
    private String currency = "USD";

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void addItem(CartItem item) {
        // Check if item already exists
        for (CartItem existingItem : items) {
            if (existingItem.getItemId().equals(item.getItemId()) && 
                existingItem.getItemType().equals(item.getItemType())) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                this.updatedAt = LocalDateTime.now();
                return;
            }
        }
        items.add(item);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean removeItem(String itemId) {
        boolean removed = items.removeIf(item -> item.getItemId().equals(itemId));
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }
}

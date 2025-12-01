package com.travelease.cart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private String itemId;
    private ItemType itemType;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private Integer quantity;
    private LocalDateTime travelDate;
    private LocalDateTime returnDate;
    private String imageUrl;
    private String additionalInfo;
    private LocalDateTime addedAt;

    public enum ItemType {
        FLIGHT, HOTEL, TRAIN, BUS
    }
}

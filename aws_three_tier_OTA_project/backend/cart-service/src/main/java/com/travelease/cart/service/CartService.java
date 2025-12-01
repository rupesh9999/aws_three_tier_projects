package com.travelease.cart.service;

import com.travelease.cart.dto.AddToCartRequest;
import com.travelease.cart.dto.CartResponse;
import com.travelease.cart.model.Cart;
import com.travelease.cart.model.CartItem;
import com.travelease.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Cart> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_TTL_HOURS = 72; // Cart expires after 72 hours

    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return CartResponse.fromCart(cart);
    }

    public CartResponse addToCart(String userId, AddToCartRequest request) {
        log.info("Adding item {} to cart for user {}", request.getItemId(), userId);

        Cart cart = getOrCreateCart(userId);

        CartItem item = CartItem.builder()
                .itemId(request.getItemId())
                .itemType(CartItem.ItemType.valueOf(request.getItemType()))
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .quantity(request.getQuantity())
                .travelDate(request.getTravelDate())
                .returnDate(request.getReturnDate())
                .imageUrl(request.getImageUrl())
                .additionalInfo(request.getAdditionalInfo())
                .addedAt(LocalDateTime.now())
                .build();

        cart.addItem(item);
        saveCart(cart);

        log.info("Item added to cart. Total items: {}", cart.getTotalItems());
        return CartResponse.fromCart(cart);
    }

    public CartResponse updateItemQuantity(String userId, String itemId, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("Quantity must be at least 1");
        }

        Cart cart = getOrCreateCart(userId);
        
        boolean found = false;
        for (CartItem item : cart.getItems()) {
            if (item.getItemId().equals(itemId)) {
                item.setQuantity(quantity);
                cart.setUpdatedAt(LocalDateTime.now());
                found = true;
                break;
            }
        }

        if (!found) {
            throw new BusinessException("Item not found in cart");
        }

        saveCart(cart);
        return CartResponse.fromCart(cart);
    }

    public CartResponse removeFromCart(String userId, String itemId) {
        log.info("Removing item {} from cart for user {}", itemId, userId);

        Cart cart = getOrCreateCart(userId);
        
        if (!cart.removeItem(itemId)) {
            throw new BusinessException("Item not found in cart");
        }

        saveCart(cart);
        return CartResponse.fromCart(cart);
    }

    public CartResponse clearCart(String userId) {
        log.info("Clearing cart for user {}", userId);

        Cart cart = getOrCreateCart(userId);
        cart.clear();
        saveCart(cart);

        return CartResponse.fromCart(cart);
    }

    private Cart getOrCreateCart(String userId) {
        String key = CART_KEY_PREFIX + userId;
        Cart cart = redisTemplate.opsForValue().get(key);

        if (cart == null) {
            cart = Cart.builder()
                    .userId(userId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        return cart;
    }

    private void saveCart(String userId, Cart cart) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, cart, CART_TTL_HOURS, TimeUnit.HOURS);
    }

    private void saveCart(Cart cart) {
        saveCart(cart.getUserId(), cart);
    }
}

package com.commsec.trading.dto;

import com.commsec.trading.model.OrderSide;
import com.commsec.trading.model.OrderType;
import com.commsec.trading.model.TimeInForce;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotBlank(message = "Symbol is required")
    @Size(min = 1, max = 10, message = "Symbol must be between 1 and 10 characters")
    private String symbol;

    @NotNull(message = "Side is required")
    private OrderSide side;

    @NotNull(message = "Type is required")
    private OrderType type;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Digits(integer = 13, fraction = 2, message = "Invalid quantity format")
    private BigDecimal quantity;

    @Positive(message = "Limit price must be positive")
    @Digits(integer = 13, fraction = 4, message = "Invalid limit price format")
    private BigDecimal limitPrice;

    @Positive(message = "Stop price must be positive")
    @Digits(integer = 13, fraction = 4, message = "Invalid stop price format")
    private BigDecimal stopPrice;

    private TimeInForce timeInForce = TimeInForce.DAY;

    @Future(message = "Expiry date must be in the future")
    private Instant expireAt;
}

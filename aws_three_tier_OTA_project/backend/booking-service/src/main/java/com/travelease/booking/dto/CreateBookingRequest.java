package com.travelease.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotBlank(message = "Booking type is required")
    private String bookingType;

    @NotBlank(message = "Item ID is required")
    private String itemId;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @NotNull(message = "Travel date is required")
    @Future(message = "Travel date must be in the future")
    private LocalDateTime travelDate;

    private LocalDateTime returnDate;

    @NotEmpty(message = "At least one traveler is required")
    @Valid
    private List<TravelerRequest> travelers;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String contactPhone;

    @Size(max = 1000, message = "Special requests must not exceed 1000 characters")
    private String specialRequests;
}

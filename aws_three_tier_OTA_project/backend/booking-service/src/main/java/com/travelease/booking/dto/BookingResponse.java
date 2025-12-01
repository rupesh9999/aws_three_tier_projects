package com.travelease.booking.dto;

import com.travelease.booking.entity.Booking;
import com.travelease.booking.entity.Traveler;
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
public class BookingResponse {
    private String id;
    private String bookingReference;
    private String userId;
    private String bookingType;
    private String itemId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private Integer quantity;
    private LocalDateTime travelDate;
    private LocalDateTime returnDate;
    private List<TravelerResponse> travelers;
    private String contactEmail;
    private String contactPhone;
    private String specialRequests;
    private String paymentId;
    private String paymentStatus;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookingResponse fromEntity(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId().toString())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId().toString())
                .bookingType(booking.getBookingType().name())
                .itemId(booking.getItemId().toString())
                .status(booking.getStatus().name())
                .totalAmount(booking.getTotalAmount())
                .currency(booking.getCurrency())
                .quantity(booking.getQuantity())
                .travelDate(booking.getTravelDate())
                .returnDate(booking.getReturnDate())
                .travelers(booking.getTravelers().stream()
                        .map(TravelerResponse::fromEntity)
                        .toList())
                .contactEmail(booking.getContactEmail())
                .contactPhone(booking.getContactPhone())
                .specialRequests(booking.getSpecialRequests())
                .paymentId(booking.getPaymentId() != null ? booking.getPaymentId().toString() : null)
                .paymentStatus(booking.getPaymentStatus())
                .cancellationReason(booking.getCancellationReason())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TravelerResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String gender;
    private String passportNumber;
    private String passportExpiry;
    private String nationality;
    private String travelerType;
    private String seatPreference;
    private String mealPreference;

    public static TravelerResponse fromEntity(Traveler traveler) {
        return TravelerResponse.builder()
                .id(traveler.getId().toString())
                .firstName(traveler.getFirstName())
                .lastName(traveler.getLastName())
                .email(traveler.getEmail())
                .phone(traveler.getPhone())
                .dateOfBirth(traveler.getDateOfBirth() != null ? traveler.getDateOfBirth().toString() : null)
                .gender(traveler.getGender() != null ? traveler.getGender().name() : null)
                .passportNumber(traveler.getPassportNumber())
                .passportExpiry(traveler.getPassportExpiry() != null ? traveler.getPassportExpiry().toString() : null)
                .nationality(traveler.getNationality())
                .travelerType(traveler.getTravelerType().name())
                .seatPreference(traveler.getSeatPreference())
                .mealPreference(traveler.getMealPreference())
                .build();
    }
}

package com.travelease.booking.service;

import com.travelease.booking.dto.*;
import com.travelease.booking.entity.Booking;
import com.travelease.booking.entity.Traveler;
import com.travelease.booking.repository.BookingRepository;
import com.travelease.common.dto.PageResponse;
import com.travelease.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private static final String REFERENCE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public BookingResponse createBooking(String userId, CreateBookingRequest request) {
        log.info("Creating booking for user: {}", userId);

        String bookingReference = generateBookingReference();
        while (bookingRepository.existsByBookingReference(bookingReference)) {
            bookingReference = generateBookingReference();
        }

        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .userId(UUID.fromString(userId))
                .bookingType(Booking.BookingType.valueOf(request.getBookingType()))
                .itemId(UUID.fromString(request.getItemId()))
                .status(Booking.BookingStatus.PENDING)
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency())
                .quantity(request.getQuantity())
                .travelDate(request.getTravelDate())
                .returnDate(request.getReturnDate())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .specialRequests(request.getSpecialRequests())
                .build();

        for (TravelerRequest travelerRequest : request.getTravelers()) {
            Traveler traveler = mapToTraveler(travelerRequest);
            booking.addTraveler(traveler);
        }

        booking = bookingRepository.save(booking);
        log.info("Booking created with reference: {}", bookingReference);

        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(String bookingId, String userId) {
        Booking booking = bookingRepository.findById(UUID.fromString(bookingId))
                .orElseThrow(() -> new BusinessException("Booking not found"));

        if (!booking.getUserId().toString().equals(userId)) {
            throw new BusinessException("Booking does not belong to user");
        }

        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference, String userId) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new BusinessException("Booking not found"));

        if (!booking.getUserId().toString().equals(userId)) {
            throw new BusinessException("Booking does not belong to user");
        }

        return BookingResponse.fromEntity(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getUserBookings(String userId, int page, int size) {
        Page<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(
                UUID.fromString(userId), PageRequest.of(page, size));

        List<BookingResponse> content = bookings.getContent().stream()
                .map(BookingResponse::fromEntity)
                .toList();

        return PageResponse.of(content, bookings.getTotalElements(), bookings.getTotalPages(), page, size);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(String userId) {
        List<Booking> bookings = bookingRepository.findUpcomingBookings(
                UUID.fromString(userId), LocalDateTime.now());

        return bookings.stream()
                .map(BookingResponse::fromEntity)
                .toList();
    }

    @Transactional
    public BookingResponse confirmBooking(String bookingId, String paymentId) {
        Booking booking = bookingRepository.findById(UUID.fromString(bookingId))
                .orElseThrow(() -> new BusinessException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BusinessException("Booking cannot be confirmed in current status");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentId(UUID.fromString(paymentId));
        booking.setPaymentStatus("COMPLETED");

        booking = bookingRepository.save(booking);
        log.info("Booking {} confirmed with payment {}", booking.getBookingReference(), paymentId);

        return BookingResponse.fromEntity(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingId, String userId, String reason) {
        Booking booking = bookingRepository.findById(UUID.fromString(bookingId))
                .orElseThrow(() -> new BusinessException("Booking not found"));

        if (!booking.getUserId().toString().equals(userId)) {
            throw new BusinessException("Booking does not belong to user");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED ||
                booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BusinessException("Booking cannot be cancelled in current status");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());

        booking = bookingRepository.save(booking);
        log.info("Booking {} cancelled by user {}", booking.getBookingReference(), userId);

        return BookingResponse.fromEntity(booking);
    }

    private Traveler mapToTraveler(TravelerRequest request) {
        Traveler.TravelerBuilder builder = Traveler.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .passportNumber(request.getPassportNumber())
                .passportExpiry(request.getPassportExpiry())
                .nationality(request.getNationality())
                .seatPreference(request.getSeatPreference())
                .mealPreference(request.getMealPreference());

        if (request.getGender() != null) {
            builder.gender(Traveler.Gender.valueOf(request.getGender()));
        }
        if (request.getTravelerType() != null) {
            builder.travelerType(Traveler.TravelerType.valueOf(request.getTravelerType()));
        }

        return builder.build();
    }

    private String generateBookingReference() {
        StringBuilder sb = new StringBuilder("TE");
        for (int i = 0; i < 8; i++) {
            sb.append(REFERENCE_CHARS.charAt(random.nextInt(REFERENCE_CHARS.length())));
        }
        return sb.toString();
    }
}

package com.travelease.booking.repository;

import com.travelease.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status IN :statuses ORDER BY b.createdAt DESC")
    Page<Booking> findByUserIdAndStatusIn(
            @Param("userId") UUID userId, 
            @Param("statuses") List<Booking.BookingStatus> statuses, 
            Pageable pageable
    );

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.travelDate >= :now AND b.status = 'CONFIRMED' ORDER BY b.travelDate ASC")
    List<Booking> findUpcomingBookings(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.travelDate < :now ORDER BY b.travelDate DESC")
    Page<Booking> findPastBookings(@Param("userId") UUID userId, @Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :status, b.updatedAt = CURRENT_TIMESTAMP WHERE b.id = :bookingId")
    void updateStatus(@Param("bookingId") UUID bookingId, @Param("status") Booking.BookingStatus status);

    @Modifying
    @Query("UPDATE Booking b SET b.paymentId = :paymentId, b.paymentStatus = :paymentStatus, b.updatedAt = CURRENT_TIMESTAMP WHERE b.id = :bookingId")
    void updatePaymentInfo(
            @Param("bookingId") UUID bookingId, 
            @Param("paymentId") UUID paymentId, 
            @Param("paymentStatus") String paymentStatus
    );

    boolean existsByBookingReference(String bookingReference);
}

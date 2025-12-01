package com.travelease.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "buses", indexes = {
        @Index(name = "idx_buses_origin_dest_date", columnList = "origin, destination, departure_time"),
        @Index(name = "idx_buses_operator", columnList = "operator")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bus_number", nullable = false, length = 20)
    private String busNumber;

    @Column(nullable = false, length = 100)
    private String operator;

    @Column(name = "operator_logo", length = 500)
    private String operatorLogo;

    @Enumerated(EnumType.STRING)
    @Column(name = "bus_type", nullable = false, length = 30)
    @Builder.Default
    private BusType busType = BusType.AC_SEATER;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(name = "origin_terminal", length = 200)
    private String originTerminal;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "destination_terminal", length = 200)
    private String destinationTerminal;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(length = 500)
    private String amenities;

    @Column(name = "boarding_points", length = 1000)
    private String boardingPoints;

    @Column(name = "dropping_points", length = 1000)
    private String droppingPoints;

    @Column(name = "rest_stops")
    @Builder.Default
    private Integer restStops = 0;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean refundable = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum BusType {
        AC_SEATER, NON_AC_SEATER, AC_SLEEPER, NON_AC_SLEEPER, VOLVO, LUXURY
    }
}

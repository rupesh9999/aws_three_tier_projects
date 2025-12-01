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
@Table(name = "trains", indexes = {
        @Index(name = "idx_trains_origin_dest_date", columnList = "origin, destination, departure_time"),
        @Index(name = "idx_trains_train_number", columnList = "train_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "train_number", nullable = false, length = 20)
    private String trainNumber;

    @Column(name = "train_name", nullable = false, length = 100)
    private String trainName;

    @Column(nullable = false, length = 100)
    private String operator;

    @Column(name = "operator_logo", length = 500)
    private String operatorLogo;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(name = "origin_station", nullable = false, length = 200)
    private String originStation;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "destination_station", nullable = false, length = 200)
    private String destinationStation;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_class", nullable = false, length = 20)
    @Builder.Default
    private TravelClass travelClass = TravelClass.SECOND_CLASS;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(length = 500)
    private String amenities;

    @Column(name = "intermediate_stops")
    @Builder.Default
    private Integer intermediateStops = 0;

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

    public enum TravelClass {
        FIRST_CLASS, SECOND_CLASS, SLEEPER, BUSINESS
    }
}

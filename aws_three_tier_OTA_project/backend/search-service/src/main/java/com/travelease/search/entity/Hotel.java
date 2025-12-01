package com.travelease.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hotels", indexes = {
        @Index(name = "idx_hotels_city", columnList = "city"),
        @Index(name = "idx_hotels_name", columnList = "name"),
        @Index(name = "idx_hotels_rating", columnList = "rating")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    private Double latitude;
    private Double longitude;

    @Column(name = "star_rating")
    @Builder.Default
    private Integer starRating = 3;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 2000)
    private String images;

    @Column(length = 1000)
    private String amenities;

    @Column(name = "room_types", length = 500)
    private String roomTypes;

    @Column(name = "available_rooms", nullable = false)
    private Integer availableRooms;

    @Column(name = "total_rooms", nullable = false)
    private Integer totalRooms;

    @Column(name = "check_in_time", length = 10)
    @Builder.Default
    private String checkInTime = "14:00";

    @Column(name = "check_out_time", length = 10)
    @Builder.Default
    private String checkOutTime = "11:00";

    @Column(name = "cancellation_policy", length = 500)
    private String cancellationPolicy;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

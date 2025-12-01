package com.travelease.search.dto;

import com.travelease.search.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponse {
    private String id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private Double latitude;
    private Double longitude;
    private Integer starRating;
    private BigDecimal rating;
    private Integer reviewCount;
    private BigDecimal pricePerNight;
    private String currency;
    private String imageUrl;
    private List<String> images;
    private List<String> amenities;
    private List<String> roomTypes;
    private Integer availableRooms;
    private String checkInTime;
    private String checkOutTime;
    private String cancellationPolicy;

    public static HotelResponse fromEntity(Hotel hotel) {
        return HotelResponse.builder()
                .id(hotel.getId().toString())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .state(hotel.getState())
                .country(hotel.getCountry())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .starRating(hotel.getStarRating())
                .rating(hotel.getRating())
                .reviewCount(hotel.getReviewCount())
                .pricePerNight(hotel.getPricePerNight())
                .currency(hotel.getCurrency())
                .imageUrl(hotel.getImageUrl())
                .images(hotel.getImages() != null ? Arrays.asList(hotel.getImages().split(",")) : List.of())
                .amenities(hotel.getAmenities() != null ? Arrays.asList(hotel.getAmenities().split(",")) : List.of())
                .roomTypes(hotel.getRoomTypes() != null ? Arrays.asList(hotel.getRoomTypes().split(",")) : List.of())
                .availableRooms(hotel.getAvailableRooms())
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .cancellationPolicy(hotel.getCancellationPolicy())
                .build();
    }
}

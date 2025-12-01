package com.travelease.search.dto;

import com.travelease.search.entity.Bus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusResponse {
    private String id;
    private String busNumber;
    private String operator;
    private String operatorLogo;
    private String busType;
    private String origin;
    private String originTerminal;
    private String destination;
    private String destinationTerminal;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private String duration;
    private BigDecimal price;
    private String currency;
    private Integer availableSeats;
    private List<String> amenities;
    private List<String> boardingPoints;
    private List<String> droppingPoints;
    private Integer restStops;
    private BigDecimal rating;
    private Integer reviewCount;
    private Boolean refundable;

    public static BusResponse fromEntity(Bus bus) {
        return BusResponse.builder()
                .id(bus.getId().toString())
                .busNumber(bus.getBusNumber())
                .operator(bus.getOperator())
                .operatorLogo(bus.getOperatorLogo())
                .busType(bus.getBusType().name())
                .origin(bus.getOrigin())
                .originTerminal(bus.getOriginTerminal())
                .destination(bus.getDestination())
                .destinationTerminal(bus.getDestinationTerminal())
                .departureTime(bus.getDepartureTime())
                .arrivalTime(bus.getArrivalTime())
                .durationMinutes(bus.getDurationMinutes())
                .duration(formatDuration(bus.getDurationMinutes()))
                .price(bus.getPrice())
                .currency(bus.getCurrency())
                .availableSeats(bus.getAvailableSeats())
                .amenities(bus.getAmenities() != null ? Arrays.asList(bus.getAmenities().split(",")) : List.of())
                .boardingPoints(bus.getBoardingPoints() != null ? Arrays.asList(bus.getBoardingPoints().split(",")) : List.of())
                .droppingPoints(bus.getDroppingPoints() != null ? Arrays.asList(bus.getDroppingPoints().split(",")) : List.of())
                .restStops(bus.getRestStops())
                .rating(bus.getRating())
                .reviewCount(bus.getReviewCount())
                .refundable(bus.getRefundable())
                .build();
    }

    private static String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%dh %dm", hours, mins);
    }
}

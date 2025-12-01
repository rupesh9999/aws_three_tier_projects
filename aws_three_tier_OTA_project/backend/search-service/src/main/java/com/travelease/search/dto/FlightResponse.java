package com.travelease.search.dto;

import com.travelease.search.entity.Flight;
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
public class FlightResponse {
    private String id;
    private String flightNumber;
    private String airline;
    private String airlineLogo;
    private String origin;
    private String originCode;
    private String destination;
    private String destinationCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private String duration;
    private BigDecimal price;
    private String currency;
    private String cabinClass;
    private Integer availableSeats;
    private Integer stops;
    private List<String> amenities;
    private String baggageAllowance;
    private Boolean refundable;

    public static FlightResponse fromEntity(Flight flight) {
        return FlightResponse.builder()
                .id(flight.getId().toString())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .airlineLogo(flight.getAirlineLogo())
                .origin(flight.getOrigin())
                .originCode(flight.getOriginCode())
                .destination(flight.getDestination())
                .destinationCode(flight.getDestinationCode())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .durationMinutes(flight.getDurationMinutes())
                .duration(formatDuration(flight.getDurationMinutes()))
                .price(flight.getPrice())
                .currency(flight.getCurrency())
                .cabinClass(flight.getCabinClass().name())
                .availableSeats(flight.getAvailableSeats())
                .stops(flight.getStops())
                .amenities(flight.getAmenities() != null ? Arrays.asList(flight.getAmenities().split(",")) : List.of())
                .baggageAllowance(flight.getBaggageAllowance())
                .refundable(flight.getRefundable())
                .build();
    }

    private static String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%dh %dm", hours, mins);
    }
}

package com.travelease.search.dto;

import com.travelease.search.entity.Train;
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
public class TrainResponse {
    private String id;
    private String trainNumber;
    private String trainName;
    private String operator;
    private String operatorLogo;
    private String origin;
    private String originStation;
    private String destination;
    private String destinationStation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private String duration;
    private BigDecimal price;
    private String currency;
    private String travelClass;
    private Integer availableSeats;
    private List<String> amenities;
    private Integer intermediateStops;
    private Boolean refundable;

    public static TrainResponse fromEntity(Train train) {
        return TrainResponse.builder()
                .id(train.getId().toString())
                .trainNumber(train.getTrainNumber())
                .trainName(train.getTrainName())
                .operator(train.getOperator())
                .operatorLogo(train.getOperatorLogo())
                .origin(train.getOrigin())
                .originStation(train.getOriginStation())
                .destination(train.getDestination())
                .destinationStation(train.getDestinationStation())
                .departureTime(train.getDepartureTime())
                .arrivalTime(train.getArrivalTime())
                .durationMinutes(train.getDurationMinutes())
                .duration(formatDuration(train.getDurationMinutes()))
                .price(train.getPrice())
                .currency(train.getCurrency())
                .travelClass(train.getTravelClass().name())
                .availableSeats(train.getAvailableSeats())
                .amenities(train.getAmenities() != null ? Arrays.asList(train.getAmenities().split(",")) : List.of())
                .intermediateStops(train.getIntermediateStops())
                .refundable(train.getRefundable())
                .build();
    }

    private static String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%dh %dm", hours, mins);
    }
}

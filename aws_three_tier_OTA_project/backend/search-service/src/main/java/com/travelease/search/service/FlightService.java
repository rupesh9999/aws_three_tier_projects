package com.travelease.search.service;

import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.FlightResponse;
import com.travelease.search.dto.FlightSearchRequest;
import com.travelease.search.entity.Flight;
import com.travelease.search.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "flightSearch", key = "#request.hashCode()")
    public PageResponse<FlightResponse> searchFlights(FlightSearchRequest request) {
        log.info("Searching flights from {} to {} on {}", 
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        LocalDateTime departureStart = request.getDepartureDate().atStartOfDay();
        LocalDateTime departureEnd = request.getDepartureDate().plusDays(1).atStartOfDay();

        Page<Flight> flights;

        if (hasFilters(request)) {
            Flight.CabinClass cabinClass = request.getCabinClass() != null 
                    ? Flight.CabinClass.valueOf(request.getCabinClass()) : null;

            flights = flightRepository.searchFlightsWithFilters(
                    request.getOrigin(),
                    request.getDestination(),
                    departureStart,
                    departureEnd,
                    request.getPassengers(),
                    Optional.ofNullable(request.getMinPrice()).orElse(BigDecimal.ZERO),
                    Optional.ofNullable(request.getMaxPrice()).orElse(new BigDecimal("99999999")),
                    cabinClass,
                    request.getMaxStops(),
                    pageRequest
            );
        } else {
            flights = flightRepository.searchFlights(
                    request.getOrigin(),
                    request.getDestination(),
                    departureStart,
                    departureEnd,
                    request.getPassengers(),
                    pageRequest
            );
        }

        List<FlightResponse> content = flights.getContent().stream()
                .map(FlightResponse::fromEntity)
                .toList();

        return PageResponse.of(content, flights);
    }

    @Transactional(readOnly = true)
    public Optional<FlightResponse> getFlightById(String id) {
        return flightRepository.findById(UUID.fromString(id))
                .map(FlightResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "airlines")
    public List<String> getAirlines() {
        return flightRepository.findDistinctAirlines();
    }

    private boolean hasFilters(FlightSearchRequest request) {
        return request.getMinPrice() != null 
                || request.getMaxPrice() != null 
                || request.getCabinClass() != null 
                || request.getMaxStops() != null;
    }
}

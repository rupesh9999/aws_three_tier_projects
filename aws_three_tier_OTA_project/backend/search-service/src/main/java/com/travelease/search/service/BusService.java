package com.travelease.search.service;

import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.BusResponse;
import com.travelease.search.dto.BusSearchRequest;
import com.travelease.search.entity.Bus;
import com.travelease.search.repository.BusRepository;
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
public class BusService {

    private final BusRepository busRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "busSearch", key = "#request.hashCode()")
    public PageResponse<BusResponse> searchBuses(BusSearchRequest request) {
        log.info("Searching buses from {} to {} on {}", 
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        LocalDateTime departureStart = request.getDepartureDate().atStartOfDay();
        LocalDateTime departureEnd = request.getDepartureDate().plusDays(1).atStartOfDay();

        Page<Bus> buses;

        if (hasFilters(request)) {
            Bus.BusType busType = request.getBusType() != null 
                    ? Bus.BusType.valueOf(request.getBusType()) : null;

            buses = busRepository.searchBusesWithFilters(
                    request.getOrigin(),
                    request.getDestination(),
                    departureStart,
                    departureEnd,
                    request.getPassengers(),
                    Optional.ofNullable(request.getMinPrice()).orElse(BigDecimal.ZERO),
                    Optional.ofNullable(request.getMaxPrice()).orElse(new BigDecimal("99999999")),
                    busType,
                    pageRequest
            );
        } else {
            buses = busRepository.searchBuses(
                    request.getOrigin(),
                    request.getDestination(),
                    departureStart,
                    departureEnd,
                    request.getPassengers(),
                    pageRequest
            );
        }

        List<BusResponse> content = buses.getContent().stream()
                .map(BusResponse::fromEntity)
                .toList();

        return PageResponse.of(content, buses);
    }

    @Transactional(readOnly = true)
    public Optional<BusResponse> getBusById(String id) {
        return busRepository.findById(UUID.fromString(id))
                .map(BusResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "popularBuses")
    public List<BusResponse> getPopularBuses() {
        return busRepository.findTop10ByActiveTrueOrderByRatingDesc().stream()
                .map(BusResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "busOperators")
    public List<String> getOperators() {
        return busRepository.findDistinctOperators();
    }

    private boolean hasFilters(BusSearchRequest request) {
        return request.getMinPrice() != null 
                || request.getMaxPrice() != null 
                || request.getBusType() != null;
    }
}

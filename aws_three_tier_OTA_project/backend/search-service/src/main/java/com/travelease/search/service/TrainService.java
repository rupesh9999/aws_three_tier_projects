package com.travelease.search.service;

import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.TrainResponse;
import com.travelease.search.dto.TrainSearchRequest;
import com.travelease.search.entity.Train;
import com.travelease.search.repository.TrainRepository;
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
public class TrainService {

    private final TrainRepository trainRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "trainSearch", key = "#request.hashCode()")
    public PageResponse<TrainResponse> searchTrains(TrainSearchRequest request) {
        log.info("Searching trains from {} to {} on {}", 
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());
        LocalDateTime departureStart = request.getDepartureDate().atStartOfDay();
        LocalDateTime departureEnd = request.getDepartureDate().plusDays(1).atStartOfDay();

        Page<Train> trains;

        if (hasFilters(request)) {
            Train.TravelClass travelClass = request.getTravelClass() != null 
                    ? Train.TravelClass.valueOf(request.getTravelClass()) : null;

            trains = trainRepository.searchTrainsWithFilters(
                    request.getOrigin(),
                    request.getDestination(),
                    departureStart,
                    departureEnd,
                    request.getPassengers(),
                    Optional.ofNullable(request.getMinPrice()).orElse(BigDecimal.ZERO),
                    Optional.ofNullable(request.getMaxPrice()).orElse(new BigDecimal("99999999")),
                    travelClass,
                    pageRequest
            );
        } else {
            trains = trainRepository.searchTrains(
                    request.getOrigin(),
                    request.getDestination(),
                    departureStart,
                    departureEnd,
                    request.getPassengers(),
                    pageRequest
            );
        }

        List<TrainResponse> content = trains.getContent().stream()
                .map(TrainResponse::fromEntity)
                .toList();

        return PageResponse.of(content, trains);
    }

    @Transactional(readOnly = true)
    public Optional<TrainResponse> getTrainById(String id) {
        return trainRepository.findById(UUID.fromString(id))
                .map(TrainResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "trainOperators")
    public List<String> getOperators() {
        return trainRepository.findDistinctOperators();
    }

    private boolean hasFilters(TrainSearchRequest request) {
        return request.getMinPrice() != null 
                || request.getMaxPrice() != null 
                || request.getTravelClass() != null;
    }
}

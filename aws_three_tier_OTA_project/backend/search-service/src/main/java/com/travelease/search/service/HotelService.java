package com.travelease.search.service;

import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.HotelResponse;
import com.travelease.search.dto.HotelSearchRequest;
import com.travelease.search.entity.Hotel;
import com.travelease.search.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "hotelSearch", key = "#request.hashCode()")
    public PageResponse<HotelResponse> searchHotels(HotelSearchRequest request) {
        log.info("Searching hotels in {} for {} rooms", request.getCity(), request.getRooms());

        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize());

        Page<Hotel> hotels;

        if (hasFilters(request)) {
            hotels = hotelRepository.searchHotelsWithFilters(
                    request.getCity(),
                    request.getRooms(),
                    Optional.ofNullable(request.getMinPrice()).orElse(BigDecimal.ZERO),
                    Optional.ofNullable(request.getMaxPrice()).orElse(new BigDecimal("99999999")),
                    request.getStarRating(),
                    request.getMinRating(),
                    pageRequest
            );
        } else {
            hotels = hotelRepository.searchHotels(
                    request.getCity(),
                    request.getRooms(),
                    pageRequest
            );
        }

        List<HotelResponse> content = hotels.getContent().stream()
                .map(HotelResponse::fromEntity)
                .toList();

        return PageResponse.of(content, hotels);
    }

    @Transactional(readOnly = true)
    public Optional<HotelResponse> getHotelById(String id) {
        return hotelRepository.findById(UUID.fromString(id))
                .map(HotelResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "popularHotels")
    public List<HotelResponse> getPopularHotels() {
        return hotelRepository.findTop10ByActiveTrueOrderByRatingDesc().stream()
                .map(HotelResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "hotelCities")
    public List<String> getCities() {
        return hotelRepository.findDistinctCities();
    }

    private boolean hasFilters(HotelSearchRequest request) {
        return request.getMinPrice() != null 
                || request.getMaxPrice() != null 
                || request.getStarRating() != null 
                || request.getMinRating() != null;
    }
}

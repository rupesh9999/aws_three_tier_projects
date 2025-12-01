package com.travelease.search.controller;

import com.travelease.common.dto.ApiResponse;
import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.HotelResponse;
import com.travelease.search.dto.HotelSearchRequest;
import com.travelease.search.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @PostMapping
    public ResponseEntity<ApiResponse<PageResponse<HotelResponse>>> searchHotels(
            @Valid @RequestBody HotelSearchRequest request) {
        PageResponse<HotelResponse> response = hotelService.searchHotels(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> getHotelById(@PathVariable String id) {
        return hotelService.getHotelById(id)
                .map(hotel -> ResponseEntity.ok(ApiResponse.success(hotel)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getPopularHotels() {
        List<HotelResponse> hotels = hotelService.getPopularHotels();
        return ResponseEntity.ok(ApiResponse.success(hotels));
    }

    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getCities() {
        List<String> cities = hotelService.getCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
}

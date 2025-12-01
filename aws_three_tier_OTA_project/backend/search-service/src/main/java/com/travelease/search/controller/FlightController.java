package com.travelease.search.controller;

import com.travelease.common.dto.ApiResponse;
import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.FlightResponse;
import com.travelease.search.dto.FlightSearchRequest;
import com.travelease.search.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> searchFlights(
            @Valid @RequestBody FlightSearchRequest request) {
        PageResponse<FlightResponse> response = flightService.searchFlights(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(@PathVariable String id) {
        return flightService.getFlightById(id)
                .map(flight -> ResponseEntity.ok(ApiResponse.success(flight)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/airlines")
    public ResponseEntity<ApiResponse<List<String>>> getAirlines() {
        List<String> airlines = flightService.getAirlines();
        return ResponseEntity.ok(ApiResponse.success(airlines));
    }
}

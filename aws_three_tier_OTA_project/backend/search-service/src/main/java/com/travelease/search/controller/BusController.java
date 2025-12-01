package com.travelease.search.controller;

import com.travelease.common.dto.ApiResponse;
import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.BusResponse;
import com.travelease.search.dto.BusSearchRequest;
import com.travelease.search.service.BusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @PostMapping
    public ResponseEntity<ApiResponse<PageResponse<BusResponse>>> searchBuses(
            @Valid @RequestBody BusSearchRequest request) {
        PageResponse<BusResponse> response = busService.searchBuses(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusResponse>> getBusById(@PathVariable String id) {
        return busService.getBusById(id)
                .map(bus -> ResponseEntity.ok(ApiResponse.success(bus)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<BusResponse>>> getPopularBuses() {
        List<BusResponse> buses = busService.getPopularBuses();
        return ResponseEntity.ok(ApiResponse.success(buses));
    }

    @GetMapping("/operators")
    public ResponseEntity<ApiResponse<List<String>>> getOperators() {
        List<String> operators = busService.getOperators();
        return ResponseEntity.ok(ApiResponse.success(operators));
    }
}

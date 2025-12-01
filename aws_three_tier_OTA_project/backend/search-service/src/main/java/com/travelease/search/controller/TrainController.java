package com.travelease.search.controller;

import com.travelease.common.dto.ApiResponse;
import com.travelease.common.dto.PageResponse;
import com.travelease.search.dto.TrainResponse;
import com.travelease.search.dto.TrainSearchRequest;
import com.travelease.search.service.TrainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/trains")
@RequiredArgsConstructor
public class TrainController {

    private final TrainService trainService;

    @PostMapping
    public ResponseEntity<ApiResponse<PageResponse<TrainResponse>>> searchTrains(
            @Valid @RequestBody TrainSearchRequest request) {
        PageResponse<TrainResponse> response = trainService.searchTrains(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainResponse>> getTrainById(@PathVariable String id) {
        return trainService.getTrainById(id)
                .map(train -> ResponseEntity.ok(ApiResponse.success(train)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/operators")
    public ResponseEntity<ApiResponse<List<String>>> getOperators() {
        List<String> operators = trainService.getOperators();
        return ResponseEntity.ok(ApiResponse.success(operators));
    }
}

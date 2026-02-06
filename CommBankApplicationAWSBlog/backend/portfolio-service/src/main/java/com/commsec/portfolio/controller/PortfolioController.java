package com.commsec.portfolio.controller;

import com.commsec.portfolio.model.Holding;
import com.commsec.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio management endpoints")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/holdings/{accountId}")
    @Operation(summary = "Get all holdings for an account")
    public ResponseEntity<List<Holding>> getHoldings(@PathVariable String accountId) {
        return ResponseEntity.ok(portfolioService.getHoldingsByAccountId(accountId));
    }

    @GetMapping("/holdings/{accountId}/{symbol}")
    @Operation(summary = "Get a specific holding")
    public ResponseEntity<Holding> getHolding(
            @PathVariable String accountId,
            @PathVariable String symbol) {
        return portfolioService.getHolding(accountId, symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary/{accountId}")
    @Operation(summary = "Get portfolio summary")
    public ResponseEntity<Map<String, Object>> getPortfolioSummary(@PathVariable String accountId) {
        return ResponseEntity.ok(portfolioService.getPortfolioSummary(accountId));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "portfolio-service"
        ));
    }
}

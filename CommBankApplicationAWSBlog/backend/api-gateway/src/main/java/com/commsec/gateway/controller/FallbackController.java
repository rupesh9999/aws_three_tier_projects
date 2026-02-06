package com.commsec.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/trading")
    public ResponseEntity<Map<String, Object>> tradingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "error",
                "message", "Trading service is temporarily unavailable. Please try again later.",
                "service", "trading-service",
                "timestamp", Instant.now().toString()
            ));
    }

    @GetMapping("/portfolio")
    public ResponseEntity<Map<String, Object>> portfolioFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "error",
                "message", "Portfolio service is temporarily unavailable. Please try again later.",
                "service", "portfolio-service",
                "timestamp", Instant.now().toString()
            ));
    }

    @GetMapping("/market")
    public ResponseEntity<Map<String, Object>> marketFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "status", "error",
                "message", "Market data service is temporarily unavailable. Please try again later.",
                "service", "market-data-service",
                "timestamp", Instant.now().toString()
            ));
    }
}

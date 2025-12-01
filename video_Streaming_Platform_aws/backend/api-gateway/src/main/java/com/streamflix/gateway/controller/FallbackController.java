package com.streamflix.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Authentication service is currently unavailable. Please try again later.",
                        "service", "auth-service"
                )));
    }
    
    @GetMapping("/content")
    public Mono<ResponseEntity<Map<String, Object>>> contentFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Content service is currently unavailable. Please try again later.",
                        "service", "content-service"
                )));
    }
    
    @GetMapping("/user")
    public Mono<ResponseEntity<Map<String, Object>>> userFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "User service is currently unavailable. Please try again later.",
                        "service", "user-service"
                )));
    }
    
    @GetMapping("/playback")
    public Mono<ResponseEntity<Map<String, Object>>> playbackFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Playback service is currently unavailable. Please try again later.",
                        "service", "playback-service"
                )));
    }
    
    @GetMapping("/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Search service is currently unavailable. Please try again later.",
                        "service", "search-service"
                )));
    }
    
    @GetMapping("/catalog")
    public Mono<ResponseEntity<Map<String, Object>>> catalogFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Catalog service is currently unavailable. Please try again later.",
                        "service", "catalog-service"
                )));
    }
    
    @GetMapping("/history")
    public Mono<ResponseEntity<Map<String, Object>>> historyFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Watch history service is currently unavailable. Please try again later.",
                        "service", "watch-history-service"
                )));
    }
    
    @GetMapping("/notification")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Notification service is currently unavailable. Please try again later.",
                        "service", "notification-service"
                )));
    }
    
    @GetMapping("/billing")
    public Mono<ResponseEntity<Map<String, Object>>> billingFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "message", "Billing service is currently unavailable. Please try again later.",
                        "service", "billing-service"
                )));
    }
}

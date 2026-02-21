package com.payment.tracking.controller;

import com.payment.common.event.PaymentEvent;
import com.payment.tracking.service.PaymentTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final PaymentTrackingService trackingService;

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentEvent> getTracking(@PathVariable UUID transactionId) {
        PaymentEvent event = trackingService.getTrackingInfo(transactionId);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<Map<UUID, PaymentEvent>> getAllTracking() {
        return ResponseEntity.ok(trackingService.getAllTracking());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Tracking Service - Healthy");
    }
}

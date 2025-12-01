package com.streamflix.billing.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamflix.billing.service.PaymentService;
import com.streamflix.billing.service.StripeService;
import com.streamflix.billing.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Stripe webhook endpoints")
public class WebhookController {
    
    private final StripeService stripeService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    
    @PostMapping
    @Operation(summary = "Handle Stripe webhook events")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        log.info("Received Stripe webhook");
        
        // Verify signature
        if (!stripeService.verifyWebhookSignature(payload, signature)) {
            log.warn("Invalid Stripe webhook signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }
        
        try {
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.get("type").asText();
            JsonNode data = event.get("data").get("object");
            
            log.info("Processing Stripe event: {}", eventType);
            
            switch (eventType) {
                case "customer.subscription.created":
                case "customer.subscription.updated":
                    handleSubscriptionUpdated(data);
                    break;
                    
                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(data);
                    break;
                    
                case "invoice.paid":
                    handleInvoicePaid(data);
                    break;
                    
                case "invoice.payment_failed":
                    handleInvoicePaymentFailed(data);
                    break;
                    
                case "payment_intent.succeeded":
                    handlePaymentSucceeded(data);
                    break;
                    
                case "payment_intent.payment_failed":
                    handlePaymentFailed(data);
                    break;
                    
                default:
                    log.debug("Unhandled event type: {}", eventType);
            }
            
            return ResponseEntity.ok("Webhook processed");
            
        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }
    
    private void handleSubscriptionUpdated(JsonNode data) {
        String subscriptionId = data.get("id").asText();
        String status = data.get("status").asText();
        
        log.info("Subscription updated: {} with status: {}", subscriptionId, status);
        
        if ("active".equals(status)) {
            subscriptionService.processSubscriptionRenewal(subscriptionId);
        } else if ("past_due".equals(status)) {
            subscriptionService.handlePaymentFailed(subscriptionId);
        }
    }
    
    private void handleSubscriptionDeleted(JsonNode data) {
        String subscriptionId = data.get("id").asText();
        log.info("Subscription deleted: {}", subscriptionId);
        subscriptionService.expireSubscription(subscriptionId);
    }
    
    private void handleInvoicePaid(JsonNode data) {
        String subscriptionId = data.has("subscription") ? data.get("subscription").asText() : null;
        log.info("Invoice paid for subscription: {}", subscriptionId);
        
        if (subscriptionId != null) {
            subscriptionService.processSubscriptionRenewal(subscriptionId);
        }
    }
    
    private void handleInvoicePaymentFailed(JsonNode data) {
        String subscriptionId = data.has("subscription") ? data.get("subscription").asText() : null;
        log.warn("Invoice payment failed for subscription: {}", subscriptionId);
        
        if (subscriptionId != null) {
            subscriptionService.handlePaymentFailed(subscriptionId);
        }
    }
    
    private void handlePaymentSucceeded(JsonNode data) {
        String paymentIntentId = data.get("id").asText();
        log.info("Payment succeeded: {}", paymentIntentId);
        paymentService.handleStripePaymentSucceeded(paymentIntentId);
    }
    
    private void handlePaymentFailed(JsonNode data) {
        String paymentIntentId = data.get("id").asText();
        String failureCode = data.has("last_payment_error") 
                ? data.get("last_payment_error").get("code").asText() 
                : "unknown";
        String failureMessage = data.has("last_payment_error") 
                ? data.get("last_payment_error").get("message").asText() 
                : "Payment failed";
        
        log.warn("Payment failed: {} - {}", paymentIntentId, failureMessage);
        paymentService.handleStripePaymentFailed(paymentIntentId, failureCode, failureMessage);
    }
}

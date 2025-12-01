package com.streamflix.billing.service;

import com.streamflix.billing.dto.PaymentMethodRequest;
import com.streamflix.billing.dto.PaymentMethodResponse;
import com.streamflix.billing.dto.PaymentResponse;
import com.streamflix.billing.dto.InvoiceResponse;
import com.streamflix.billing.entity.Invoice;
import com.streamflix.billing.entity.Payment;
import com.streamflix.billing.entity.PaymentMethod;
import com.streamflix.billing.repository.InvoiceRepository;
import com.streamflix.billing.repository.PaymentMethodRepository;
import com.streamflix.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final InvoiceRepository invoiceRepository;
    private final StripeService stripeService;
    
    // ==================== Payment Methods ====================
    
    public PaymentMethodResponse addPaymentMethod(UUID userId, PaymentMethodRequest request) {
        log.info("Adding payment method for user: {}", userId);
        
        // Attach to Stripe
        String stripePaymentMethodId = stripeService.attachPaymentMethod(
                userId.toString(),
                request.getPaymentMethodToken()
        );
        
        // Get card details from Stripe
        StripeService.PaymentMethodDetails details = stripeService.getPaymentMethodDetails(stripePaymentMethodId);
        
        // If this is to be the default, unset other defaults
        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(pm -> {
                        pm.setIsDefault(false);
                        paymentMethodRepository.save(pm);
                    });
        }
        
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .userId(userId)
                .type(PaymentMethod.PaymentMethodType.valueOf(request.getType().toUpperCase()))
                .stripePaymentMethodId(stripePaymentMethodId)
                .cardBrand(details.brand())
                .last4(details.last4())
                .expiryMonth(details.expMonth())
                .expiryYear(details.expYear())
                .isDefault(request.getSetAsDefault())
                .isActive(true)
                .billingName(request.getBillingName())
                .billingEmail(request.getBillingEmail())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();
        
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        log.info("Added payment method: {} for user: {}", paymentMethod.getId(), userId);
        
        return PaymentMethodResponse.fromEntity(paymentMethod);
    }
    
    public List<PaymentMethodResponse> getUserPaymentMethods(UUID userId) {
        return paymentMethodRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(PaymentMethodResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    public PaymentMethodResponse getPaymentMethod(UUID paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + paymentMethodId));
        return PaymentMethodResponse.fromEntity(paymentMethod);
    }
    
    public PaymentMethodResponse setDefaultPaymentMethod(UUID userId, UUID paymentMethodId) {
        log.info("Setting default payment method: {} for user: {}", paymentMethodId, userId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + paymentMethodId));
        
        if (!paymentMethod.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Payment method does not belong to user");
        }
        
        // Unset current default
        paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(pm -> {
                    pm.setIsDefault(false);
                    paymentMethodRepository.save(pm);
                });
        
        paymentMethod.setIsDefault(true);
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        
        return PaymentMethodResponse.fromEntity(paymentMethod);
    }
    
    public void deletePaymentMethod(UUID userId, UUID paymentMethodId) {
        log.info("Deleting payment method: {} for user: {}", paymentMethodId, userId);
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + paymentMethodId));
        
        if (!paymentMethod.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Payment method does not belong to user");
        }
        
        if (Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
            throw new IllegalStateException("Cannot delete default payment method. Set another as default first.");
        }
        
        // Delete from Stripe
        stripeService.deletePaymentMethod(paymentMethod.getStripePaymentMethodId());
        
        // Soft delete
        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);
        
        log.info("Deleted payment method: {}", paymentMethodId);
    }
    
    // ==================== Invoices ====================
    
    public Page<InvoiceResponse> getUserInvoices(UUID userId, Pageable pageable) {
        return invoiceRepository.findByUserIdOrderByIssuedAtDesc(userId, pageable)
                .map(InvoiceResponse::fromEntity);
    }
    
    public InvoiceResponse getInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        return InvoiceResponse.fromEntity(invoice);
    }
    
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceNumber));
        return InvoiceResponse.fromEntity(invoice);
    }
    
    // ==================== Payments ====================
    
    public Page<PaymentResponse> getUserPayments(UUID userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(PaymentResponse::fromEntity);
    }
    
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        return PaymentResponse.fromEntity(payment);
    }
    
    public PaymentResponse processPayment(UUID userId, UUID invoiceId, UUID paymentMethodId) {
        log.info("Processing payment for invoice: {} with payment method: {}", invoiceId, paymentMethodId);
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        
        if (!invoice.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Invoice does not belong to user");
        }
        
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already paid");
        }
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + paymentMethodId));
        
        if (!paymentMethod.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Payment method does not belong to user");
        }
        
        // Create payment intent in Stripe
        String paymentIntentId = stripeService.createPaymentIntent(
                userId.toString(),
                invoice.getTotal(),
                invoice.getCurrency(),
                paymentMethod.getStripePaymentMethodId()
        );
        
        Payment payment = Payment.builder()
                .userId(userId)
                .invoice(invoice)
                .paymentMethod(paymentMethod)
                .amount(invoice.getTotal())
                .currency(invoice.getCurrency())
                .status(Payment.PaymentStatus.PENDING)
                .paymentIntentId(paymentIntentId)
                .build();
        
        payment = paymentRepository.save(payment);
        
        try {
            // Confirm payment
            String chargeId = stripeService.confirmPaymentIntent(paymentIntentId);
            
            payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
            payment.setChargeId(chargeId);
            payment.setProcessedAt(LocalDateTime.now());
            payment.setReceiptUrl("https://stripe.com/receipts/" + chargeId);
            
            // Update invoice
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            
            log.info("Payment succeeded for invoice: {}", invoiceId);
        } catch (Exception e) {
            log.error("Payment failed for invoice: {}", invoiceId, e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureCode("payment_failed");
            payment.setFailureMessage(e.getMessage());
        }
        
        payment = paymentRepository.save(payment);
        return PaymentResponse.fromEntity(payment);
    }
    
    public PaymentResponse refundPayment(UUID userId, UUID paymentId, BigDecimal amount) {
        log.info("Processing refund for payment: {}, amount: {}", paymentId, amount);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        
        if (!payment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Payment does not belong to user");
        }
        
        if (payment.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Can only refund succeeded payments");
        }
        
        BigDecimal refundAmount = amount != null ? amount : payment.getAmount();
        
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }
        
        // Process refund in Stripe
        stripeService.refundPayment(payment.getChargeId(), refundAmount);
        
        if (refundAmount.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
        }
        
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundedAmount(
                payment.getRefundedAmount() != null 
                        ? payment.getRefundedAmount().add(refundAmount) 
                        : refundAmount
        );
        
        payment = paymentRepository.save(payment);
        log.info("Refund processed for payment: {}", paymentId);
        
        return PaymentResponse.fromEntity(payment);
    }
    
    // ==================== Webhook Handlers ====================
    
    public void handleStripePaymentSucceeded(String paymentIntentId) {
        log.info("Handling payment succeeded webhook for: {}", paymentIntentId);
        
        paymentRepository.findByPaymentIntentId(paymentIntentId)
                .ifPresent(payment -> {
                    payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
                    payment.setProcessedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                });
    }
    
    public void handleStripePaymentFailed(String paymentIntentId, String failureCode, String failureMessage) {
        log.warn("Handling payment failed webhook for: {}", paymentIntentId);
        
        paymentRepository.findByPaymentIntentId(paymentIntentId)
                .ifPresent(payment -> {
                    payment.setStatus(Payment.PaymentStatus.FAILED);
                    payment.setFailureCode(failureCode);
                    payment.setFailureMessage(failureMessage);
                    paymentRepository.save(payment);
                });
    }
}

package com.streamflix.billing.dto;

import com.streamflix.billing.entity.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    
    private UUID id;
    private UUID userId;
    private UUID subscriptionId;
    private String invoiceNumber;
    private Invoice.InvoiceStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String currency;
    private LocalDateTime issuedAt;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private String description;
    private String periodStart;
    private String periodEnd;
    private String stripeInvoiceId;
    private String invoiceUrl;
    private String pdfUrl;
    private LocalDateTime createdAt;
    
    public static InvoiceResponse fromEntity(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .userId(invoice.getUserId())
                .subscriptionId(invoice.getSubscription() != null ? invoice.getSubscription().getId() : null)
                .invoiceNumber(invoice.getInvoiceNumber())
                .status(invoice.getStatus())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .discount(invoice.getDiscount())
                .total(invoice.getTotal())
                .currency(invoice.getCurrency())
                .issuedAt(invoice.getIssuedAt())
                .dueDate(invoice.getDueDate())
                .paidAt(invoice.getPaidAt())
                .description(invoice.getDescription())
                .periodStart(invoice.getPeriodStart())
                .periodEnd(invoice.getPeriodEnd())
                .stripeInvoiceId(invoice.getStripeInvoiceId())
                .invoiceUrl(invoice.getInvoiceUrl())
                .pdfUrl(invoice.getPdfUrl())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}

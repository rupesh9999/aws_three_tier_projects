package com.streamflix.billing.repository;

import com.streamflix.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Invoice> findByUserIdOrderByIssuedAtDesc(UUID userId, Pageable pageable);

    Optional<Invoice> findByIdAndUserId(UUID id, UUID userId);

    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findBySubscriptionId(UUID subscriptionId);

    List<Invoice> findByStatusAndDueDateBefore(Invoice.InvoiceStatus status, LocalDateTime date);

    Page<Invoice> findByStatus(Invoice.InvoiceStatus status, Pageable pageable);

    long countByUserIdAndStatus(UUID userId, Invoice.InvoiceStatus status);
}

package com.streamflix.billing.repository;

import com.streamflix.billing.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    Optional<Payment> findByChargeId(String chargeId);

    List<Payment> findByInvoiceId(UUID invoiceId);

    List<Payment> findByUserIdAndStatus(UUID userId, Payment.PaymentStatus status);

    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);
}

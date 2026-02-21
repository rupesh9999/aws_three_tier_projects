package com.payment.initiation.repository;

import com.payment.common.model.Payment;
import com.payment.common.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKeyAndTenantId(String idempotencyKey, String tenantId);

    Page<Payment> findByTenantId(String tenantId, Pageable pageable);

    Page<Payment> findByTenantIdAndStatus(String tenantId, PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId AND p.status IN :statuses ORDER BY p.createdAt DESC")
    List<Payment> findByTenantIdAndStatusIn(String tenantId, List<PaymentStatus> statuses);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.tenantId = :tenantId AND p.status = :status")
    long countByTenantIdAndStatus(String tenantId, PaymentStatus status);
}

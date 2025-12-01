package com.streamflix.billing.repository;

import com.streamflix.billing.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    List<PaymentMethod> findByUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    List<PaymentMethod> findByUserIdAndIsActiveTrue(UUID userId);

    Optional<PaymentMethod> findByIdAndUserId(UUID id, UUID userId);

    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(UUID userId);

    Optional<PaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    @Modifying
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.userId = :userId AND p.id != :id")
    void clearDefaultForUser(@Param("userId") UUID userId, @Param("id") UUID id);

    void deleteByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}

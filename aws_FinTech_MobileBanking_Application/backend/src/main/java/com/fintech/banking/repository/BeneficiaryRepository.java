package com.fintech.banking.repository;

import com.fintech.banking.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByUserId(UUID userId);

    List<Beneficiary> findByUserIdAndStatus(UUID userId, Beneficiary.BeneficiaryStatus status);

    @Query("SELECT b FROM Beneficiary b WHERE b.user.id = :userId AND b.accountNumber = :accountNumber")
    Optional<Beneficiary> findByUserIdAndAccountNumber(@Param("userId") UUID userId, 
                                                        @Param("accountNumber") String accountNumber);

    boolean existsByUserIdAndAccountNumber(UUID userId, String accountNumber);

    @Query("SELECT b FROM Beneficiary b WHERE b.user.email = :email AND b.status = 'ACTIVE'")
    List<Beneficiary> findActiveBeneficiariesByUserEmail(@Param("email") String email);

    @Query("SELECT b FROM Beneficiary b WHERE b.user.id = :userId " +
           "AND (LOWER(b.nickname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(b.accountHolderName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Beneficiary> searchByUserIdAndTerm(@Param("userId") UUID userId, 
                                             @Param("searchTerm") String searchTerm);
}

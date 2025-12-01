package com.fintech.banking.repository;

import com.fintech.banking.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    Page<Transaction> findByFromAccountIdOrToAccountId(UUID fromAccountId, UUID toAccountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") UUID accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.fromAccount.id = :accountId " +
           "AND t.transactionType = 'DEBIT' " +
           "AND t.status = 'SUCCESS' " +
           "AND t.createdAt >= :startOfDay")
    BigDecimal getTotalDebitAmountToday(
            @Param("accountId") UUID accountId,
            @Param("startOfDay") LocalDateTime startOfDay);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.user.id = :userId OR t.toAccount.user.id = :userId " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    List<Transaction> findTop10ByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(UUID fromAccountId, UUID toAccountId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromAccount.id = :accountId " +
           "AND t.status = 'SUCCESS' AND t.createdAt >= :startDate")
    Long countSuccessfulTransactionsSince(@Param("accountId") UUID accountId, 
                                          @Param("startDate") LocalDateTime startDate);
}

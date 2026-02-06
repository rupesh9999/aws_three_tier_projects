package com.commsec.trading.repository;

import com.commsec.trading.model.Order;
import com.commsec.trading.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByAccountId(String accountId, Pageable pageable);

    Page<Order> findByAccountIdAndStatus(String accountId, OrderStatus status, Pageable pageable);

    List<Order> findByAccountIdAndStatusIn(String accountId, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.accountId = :accountId AND o.symbol = :symbol " +
           "ORDER BY o.createdAt DESC")
    Page<Order> findByAccountIdAndSymbol(
        @Param("accountId") String accountId,
        @Param("symbol") String symbol,
        Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.expireAt < :now")
    List<Order> findExpiredOrders(@Param("status") OrderStatus status, @Param("now") Instant now);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.accountId = :accountId AND o.status IN :statuses")
    long countOpenOrdersByAccountId(
        @Param("accountId") String accountId,
        @Param("statuses") List<OrderStatus> statuses
    );

    Optional<Order> findByIdAndAccountId(UUID id, String accountId);

    Optional<Order> findByExternalOrderId(String externalOrderId);
}

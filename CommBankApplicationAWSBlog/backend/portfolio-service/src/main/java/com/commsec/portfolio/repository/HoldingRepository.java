package com.commsec.portfolio.repository;

import com.commsec.portfolio.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, String> {
    List<Holding> findByAccountId(String accountId);
    Optional<Holding> findByAccountIdAndSymbol(String accountId, String symbol);
}

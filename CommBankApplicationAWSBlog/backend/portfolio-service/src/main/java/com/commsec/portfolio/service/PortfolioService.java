package com.commsec.portfolio.service;

import com.commsec.portfolio.model.Holding;
import com.commsec.portfolio.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;

    public List<Holding> getHoldingsByAccountId(String accountId) {
        return holdingRepository.findByAccountId(accountId);
    }

    public Optional<Holding> getHolding(String accountId, String symbol) {
        return holdingRepository.findByAccountIdAndSymbol(accountId, symbol);
    }

    public Map<String, Object> getPortfolioSummary(String accountId) {
        List<Holding> holdings = holdingRepository.findByAccountId(accountId);
        
        BigDecimal totalValue = holdings.stream()
                .map(h -> h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCost = holdings.stream()
                .map(Holding::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPnl = totalValue.subtract(totalCost);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("accountId", accountId);
        summary.put("holdingsCount", holdings.size());
        summary.put("totalValue", totalValue);
        summary.put("totalCost", totalCost);
        summary.put("totalPnl", totalPnl);
        summary.put("holdings", holdings);
        
        return summary;
    }
}

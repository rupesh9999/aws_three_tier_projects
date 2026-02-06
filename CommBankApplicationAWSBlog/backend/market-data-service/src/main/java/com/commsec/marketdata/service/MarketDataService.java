package com.commsec.marketdata.service;

import com.commsec.marketdata.model.StockQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MarketDataService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, StockQuote> stockCache = new ConcurrentHashMap<>();

    private static final List<Map<String, Object>> ASX_STOCKS = List.of(
        Map.of("symbol", "CBA", "name", "Commonwealth Bank of Australia", "basePrice", 134.28),
        Map.of("symbol", "BHP", "name", "BHP Group Ltd", "basePrice", 45.67),
        Map.of("symbol", "CSL", "name", "CSL Limited", "basePrice", 289.45),
        Map.of("symbol", "WBC", "name", "Westpac Banking Corporation", "basePrice", 28.94),
        Map.of("symbol", "NAB", "name", "National Australia Bank", "basePrice", 35.12),
        Map.of("symbol", "ANZ", "name", "ANZ Group Holdings Ltd", "basePrice", 29.45),
        Map.of("symbol", "WES", "name", "Wesfarmers Ltd", "basePrice", 73.82),
        Map.of("symbol", "MQG", "name", "Macquarie Group Ltd", "basePrice", 198.50),
        Map.of("symbol", "RIO", "name", "Rio Tinto Ltd", "basePrice", 118.45),
        Map.of("symbol", "TLS", "name", "Telstra Group Ltd", "basePrice", 3.92)
    );

    public MarketDataService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        initializeCache();
    }

    private void initializeCache() {
        for (Map<String, Object> stock : ASX_STOCKS) {
            String symbol = (String) stock.get("symbol");
            double basePrice = (Double) stock.get("basePrice");
            stockCache.put(symbol, generateQuote(symbol, (String) stock.get("name"), basePrice));
        }
    }

    @Scheduled(fixedRate = 1000)
    public void publishMarketUpdates() {
        List<StockQuote> updates = new ArrayList<>();
        
        for (Map<String, Object> stock : ASX_STOCKS) {
            if (ThreadLocalRandom.current().nextDouble() < 0.3) { // 30% chance of update
                String symbol = (String) stock.get("symbol");
                StockQuote currentQuote = stockCache.get(symbol);
                StockQuote newQuote = updateQuote(currentQuote);
                stockCache.put(symbol, newQuote);
                updates.add(newQuote);
            }
        }
        
        if (!updates.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/quotes", updates);
            log.debug("Published {} quote updates", updates.size());
        }
    }

    public StockQuote getQuote(String symbol) {
        return stockCache.get(symbol.toUpperCase());
    }

    public List<StockQuote> getAllQuotes() {
        return new ArrayList<>(stockCache.values());
    }

    public List<StockQuote> getQuotes(List<String> symbols) {
        return symbols.stream()
            .map(String::toUpperCase)
            .filter(stockCache::containsKey)
            .map(stockCache::get)
            .toList();
    }

    private StockQuote generateQuote(String symbol, String name, double basePrice) {
        BigDecimal price = BigDecimal.valueOf(basePrice);
        BigDecimal change = randomChange(price);
        BigDecimal changePercent = change.divide(price, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        return StockQuote.builder()
            .symbol(symbol)
            .companyName(name)
            .lastPrice(price)
            .change(change)
            .changePercent(changePercent)
            .open(price.subtract(randomChange(price)))
            .high(price.add(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0.5, 2.0))))
            .low(price.subtract(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0.5, 2.0))))
            .previousClose(price.subtract(change))
            .volume(ThreadLocalRandom.current().nextLong(1_000_000, 50_000_000))
            .marketCap(price.multiply(BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000, 200_000_000_000L))).longValue())
            .timestamp(Instant.now())
            .build();
    }

    private StockQuote updateQuote(StockQuote current) {
        BigDecimal priceChange = randomSmallChange(current.getLastPrice());
        BigDecimal newPrice = current.getLastPrice().add(priceChange);
        BigDecimal newChange = newPrice.subtract(current.getPreviousClose());
        BigDecimal newChangePercent = newChange.divide(current.getPreviousClose(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        return StockQuote.builder()
            .symbol(current.getSymbol())
            .companyName(current.getCompanyName())
            .lastPrice(newPrice)
            .change(newChange)
            .changePercent(newChangePercent)
            .open(current.getOpen())
            .high(newPrice.max(current.getHigh()))
            .low(newPrice.min(current.getLow()))
            .previousClose(current.getPreviousClose())
            .volume(current.getVolume() + ThreadLocalRandom.current().nextLong(10_000, 500_000))
            .marketCap(current.getMarketCap())
            .timestamp(Instant.now())
            .build();
    }

    private BigDecimal randomChange(BigDecimal price) {
        double percentChange = ThreadLocalRandom.current().nextDouble(-3.0, 3.0);
        return price.multiply(BigDecimal.valueOf(percentChange / 100))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal randomSmallChange(BigDecimal price) {
        double percentChange = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        return price.multiply(BigDecimal.valueOf(percentChange / 100))
            .setScale(4, RoundingMode.HALF_UP);
    }
}

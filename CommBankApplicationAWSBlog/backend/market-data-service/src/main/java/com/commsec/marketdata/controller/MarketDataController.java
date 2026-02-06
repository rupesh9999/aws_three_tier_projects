package com.commsec.marketdata.controller;

import com.commsec.marketdata.model.StockQuote;
import com.commsec.marketdata.service.MarketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
@Tag(name = "Market Data", description = "Stock quotes and market data endpoints")
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping("/quotes")
    @Operation(summary = "Get all available stock quotes")
    public ResponseEntity<List<StockQuote>> getAllQuotes() {
        return ResponseEntity.ok(marketDataService.getAllQuotes());
    }

    @GetMapping("/quotes/{symbol}")
    @Operation(summary = "Get quote for a specific symbol")
    public ResponseEntity<StockQuote> getQuote(@PathVariable String symbol) {
        StockQuote quote = marketDataService.getQuote(symbol);
        if (quote == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/quotes/batch")
    @Operation(summary = "Get quotes for multiple symbols")
    public ResponseEntity<List<StockQuote>> getQuotes(@RequestParam List<String> symbols) {
        return ResponseEntity.ok(marketDataService.getQuotes(symbols));
    }
}

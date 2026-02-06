package com.commsec.marketdata.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockQuote {
    private String symbol;
    private String companyName;
    private BigDecimal lastPrice;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal previousClose;
    private Long volume;
    private Long marketCap;
    private Instant timestamp;
}

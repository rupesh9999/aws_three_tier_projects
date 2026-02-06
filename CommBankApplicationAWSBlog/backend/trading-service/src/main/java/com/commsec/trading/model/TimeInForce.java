package com.commsec.trading.model;

public enum TimeInForce {
    DAY,        // Valid for the trading day
    GTC,        // Good Till Cancelled
    IOC,        // Immediate or Cancel
    FOK,        // Fill or Kill
    GTD         // Good Till Date
}

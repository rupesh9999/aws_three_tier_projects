-- V1__Initial_Schema.sql
-- CommSec Trading Platform - Database Schema

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(255),
    side VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    quantity DECIMAL(15, 2) NOT NULL,
    filled_quantity DECIMAL(15, 2) DEFAULT 0,
    limit_price DECIMAL(15, 4),
    stop_price DECIMAL(15, 4),
    avg_fill_price DECIMAL(15, 4),
    total_value DECIMAL(18, 2),
    brokerage DECIMAL(10, 2),
    time_in_force VARCHAR(10),
    expire_at TIMESTAMP WITH TIME ZONE,
    submitted_at TIMESTAMP WITH TIME ZONE,
    filled_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    rejection_reason VARCHAR(500),
    external_order_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT DEFAULT 0
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_orders_account_id ON orders(account_id);
CREATE INDEX IF NOT EXISTS idx_orders_symbol ON orders(symbol);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_account_status ON orders(account_id, status);

-- Add comments
COMMENT ON TABLE orders IS 'Trading orders table';
COMMENT ON COLUMN orders.account_id IS 'Account identifier for the order owner';
COMMENT ON COLUMN orders.symbol IS 'Stock symbol (e.g., CBA, BHP)';
COMMENT ON COLUMN orders.side IS 'BUY or SELL';
COMMENT ON COLUMN orders.type IS 'MARKET, LIMIT, STOP, STOP_LIMIT';
COMMENT ON COLUMN orders.status IS 'PENDING, OPEN, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED, EXPIRED';

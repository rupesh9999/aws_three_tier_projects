-- CommSec Trading Platform - Database Initialization Script
-- PostgreSQL schema for the trading platform

-- Create database (run as superuser if database doesn't exist)
-- CREATE DATABASE commsec;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Orders table (main trading orders)
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(255),
    side VARCHAR(10) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    type VARCHAR(20) NOT NULL CHECK (type IN ('MARKET', 'LIMIT', 'STOP', 'STOP_LIMIT')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'OPEN', 'PARTIALLY_FILLED', 'FILLED', 'CANCELLED', 'REJECTED', 'EXPIRED')),
    quantity DECIMAL(15, 2) NOT NULL CHECK (quantity > 0),
    filled_quantity DECIMAL(15, 2) DEFAULT 0 CHECK (filled_quantity >= 0),
    limit_price DECIMAL(15, 4),
    stop_price DECIMAL(15, 4),
    avg_fill_price DECIMAL(15, 4),
    total_value DECIMAL(18, 2),
    brokerage DECIMAL(10, 2),
    time_in_force VARCHAR(10) CHECK (time_in_force IN ('DAY', 'GTC', 'IOC', 'FOK', 'GTD')),
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

-- Portfolio holdings table
CREATE TABLE IF NOT EXISTS holdings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(255),
    quantity DECIMAL(15, 2) NOT NULL CHECK (quantity > 0),
    average_cost DECIMAL(15, 4) NOT NULL CHECK (average_cost > 0),
    total_cost DECIMAL(18, 2) NOT NULL,
    current_price DECIMAL(15, 4),
    current_value DECIMAL(18, 2),
    unrealized_pnl DECIMAL(18, 2),
    unrealized_pnl_percent DECIMAL(8, 4),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(account_id, symbol)
);

-- Trade executions table
CREATE TABLE IF NOT EXISTS trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    account_id VARCHAR(50) NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity DECIMAL(15, 2) NOT NULL,
    price DECIMAL(15, 4) NOT NULL,
    value DECIMAL(18, 2) NOT NULL,
    brokerage DECIMAL(10, 2),
    net_value DECIMAL(18, 2),
    executed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(20) NOT NULL UNIQUE,
    user_id VARCHAR(50) NOT NULL,
    account_type VARCHAR(20) NOT NULL DEFAULT 'TRADING',
    cash_balance DECIMAL(18, 2) NOT NULL DEFAULT 0,
    available_cash DECIMAL(18, 2) NOT NULL DEFAULT 0,
    reserved_cash DECIMAL(18, 2) NOT NULL DEFAULT 0,
    total_portfolio_value DECIMAL(18, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Watchlists table
CREATE TABLE IF NOT EXISTS watchlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(account_id, name)
);

-- Watchlist items
CREATE TABLE IF NOT EXISTS watchlist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    watchlist_id UUID NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
    symbol VARCHAR(10) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(watchlist_id, symbol)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_orders_account_id ON orders(account_id);
CREATE INDEX IF NOT EXISTS idx_orders_symbol ON orders(symbol);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_account_status ON orders(account_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_account_symbol ON orders(account_id, symbol);

CREATE INDEX IF NOT EXISTS idx_holdings_account_id ON holdings(account_id);
CREATE INDEX IF NOT EXISTS idx_holdings_symbol ON holdings(symbol);

CREATE INDEX IF NOT EXISTS idx_trades_account_id ON trades(account_id);
CREATE INDEX IF NOT EXISTS idx_trades_order_id ON trades(order_id);
CREATE INDEX IF NOT EXISTS idx_trades_executed_at ON trades(executed_at);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);

-- Grant permissions (adjust as needed for your setup)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO commsec_admin;
-- GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO commsec_app;

-- Add table comments
COMMENT ON TABLE orders IS 'Trading orders - buy and sell orders for securities';
COMMENT ON TABLE holdings IS 'Current portfolio holdings per account';
COMMENT ON TABLE trades IS 'Executed trade records';
COMMENT ON TABLE accounts IS 'User trading accounts with balance information';
COMMENT ON TABLE watchlists IS 'User-defined watchlists for tracking securities';
COMMENT ON TABLE watchlist_items IS 'Securities in each watchlist';

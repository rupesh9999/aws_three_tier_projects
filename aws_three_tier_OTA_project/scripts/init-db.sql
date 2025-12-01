-- Initialize databases for all services
-- This script runs on first PostgreSQL startup

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create separate schemas for each service (optional, for better isolation)
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS search;
CREATE SCHEMA IF NOT EXISTS booking;
CREATE SCHEMA IF NOT EXISTS payment;

-- Grant privileges
GRANT ALL PRIVILEGES ON SCHEMA auth TO admin;
GRANT ALL PRIVILEGES ON SCHEMA search TO admin;
GRANT ALL PRIVILEGES ON SCHEMA booking TO admin;
GRANT ALL PRIVILEGES ON SCHEMA payment TO admin;

-- Log initialization
SELECT 'Database initialization completed at ' || NOW();

-- StreamFlix Database Initialization Script
-- Creates all required schemas for microservices

-- Create schemas
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS content;
CREATE SCHEMA IF NOT EXISTS playback;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS billing;
CREATE SCHEMA IF NOT EXISTS notifications;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA auth TO streamflix;
GRANT ALL PRIVILEGES ON SCHEMA content TO streamflix;
GRANT ALL PRIVILEGES ON SCHEMA playback TO streamflix;
GRANT ALL PRIVILEGES ON SCHEMA users TO streamflix;
GRANT ALL PRIVILEGES ON SCHEMA billing TO streamflix;
GRANT ALL PRIVILEGES ON SCHEMA notifications TO streamflix;

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Set default search path
ALTER DATABASE streamflix SET search_path TO public, auth, content, playback, users, billing, notifications;

-- Create audit function for tracking changes
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Log initialization
DO $$
BEGIN
    RAISE NOTICE 'StreamFlix database initialized successfully';
    RAISE NOTICE 'Schemas created: auth, content, playback, users, billing, notifications';
END $$;

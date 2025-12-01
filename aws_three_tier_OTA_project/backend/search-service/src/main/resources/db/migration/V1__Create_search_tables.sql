-- V1__Create_search_tables.sql
-- Initial schema for search-service

-- Flights table
CREATE TABLE IF NOT EXISTS flights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flight_number VARCHAR(20) NOT NULL,
    airline VARCHAR(100) NOT NULL,
    airline_logo VARCHAR(500),
    origin VARCHAR(100) NOT NULL,
    origin_code VARCHAR(10) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    destination_code VARCHAR(10) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    cabin_class VARCHAR(20) NOT NULL DEFAULT 'ECONOMY',
    available_seats INTEGER NOT NULL,
    total_seats INTEGER NOT NULL,
    stops INTEGER NOT NULL DEFAULT 0,
    amenities VARCHAR(500),
    baggage_allowance VARCHAR(100),
    refundable BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_flights_origin_dest_date ON flights(origin, destination, departure_time);
CREATE INDEX IF NOT EXISTS idx_flights_airline ON flights(airline);
CREATE INDEX IF NOT EXISTS idx_flights_flight_number ON flights(flight_number);

-- Hotels table
CREATE TABLE IF NOT EXISTS hotels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    star_rating INTEGER DEFAULT 3,
    rating DECIMAL(3, 2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    price_per_night DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    image_url VARCHAR(500),
    images VARCHAR(2000),
    amenities VARCHAR(1000),
    room_types VARCHAR(500),
    available_rooms INTEGER NOT NULL,
    total_rooms INTEGER NOT NULL,
    check_in_time VARCHAR(10) DEFAULT '14:00',
    check_out_time VARCHAR(10) DEFAULT '11:00',
    cancellation_policy VARCHAR(500),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hotels_city ON hotels(city);
CREATE INDEX IF NOT EXISTS idx_hotels_name ON hotels(name);
CREATE INDEX IF NOT EXISTS idx_hotels_rating ON hotels(rating);

-- Trains table
CREATE TABLE IF NOT EXISTS trains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    train_number VARCHAR(20) NOT NULL,
    train_name VARCHAR(100) NOT NULL,
    operator VARCHAR(100) NOT NULL,
    operator_logo VARCHAR(500),
    origin VARCHAR(100) NOT NULL,
    origin_station VARCHAR(200) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    destination_station VARCHAR(200) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    travel_class VARCHAR(20) NOT NULL DEFAULT 'SECOND_CLASS',
    available_seats INTEGER NOT NULL,
    total_seats INTEGER NOT NULL,
    amenities VARCHAR(500),
    intermediate_stops INTEGER DEFAULT 0,
    refundable BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_trains_origin_dest_date ON trains(origin, destination, departure_time);
CREATE INDEX IF NOT EXISTS idx_trains_train_number ON trains(train_number);

-- Buses table
CREATE TABLE IF NOT EXISTS buses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bus_number VARCHAR(20) NOT NULL,
    operator VARCHAR(100) NOT NULL,
    operator_logo VARCHAR(500),
    bus_type VARCHAR(30) NOT NULL DEFAULT 'AC_SEATER',
    origin VARCHAR(100) NOT NULL,
    origin_terminal VARCHAR(200),
    destination VARCHAR(100) NOT NULL,
    destination_terminal VARCHAR(200),
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    available_seats INTEGER NOT NULL,
    total_seats INTEGER NOT NULL,
    amenities VARCHAR(500),
    boarding_points VARCHAR(1000),
    dropping_points VARCHAR(1000),
    rest_stops INTEGER DEFAULT 0,
    rating DECIMAL(3, 2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    refundable BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_buses_origin_dest_date ON buses(origin, destination, departure_time);
CREATE INDEX IF NOT EXISTS idx_buses_operator ON buses(operator);

-- Create update triggers
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_flights_updated_at
    BEFORE UPDATE ON flights
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_hotels_updated_at
    BEFORE UPDATE ON hotels
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_trains_updated_at
    BEFORE UPDATE ON trains
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_buses_updated_at
    BEFORE UPDATE ON buses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

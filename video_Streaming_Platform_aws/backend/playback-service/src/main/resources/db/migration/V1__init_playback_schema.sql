-- Playback Service Schema
CREATE SCHEMA IF NOT EXISTS playback;

-- Set search path
SET search_path TO playback;

-- Playback sessions table
CREATE TABLE playback_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    episode_id BIGINT,
    device_id VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    quality VARCHAR(20) DEFAULT 'AUTO',
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP WITH TIME ZONE,
    position_seconds INTEGER DEFAULT 0,
    duration_seconds INTEGER,
    is_active BOOLEAN DEFAULT true,
    ip_address VARCHAR(45),
    user_agent TEXT,
    stream_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Watch progress table
CREATE TABLE watch_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    episode_id BIGINT,
    position_seconds INTEGER NOT NULL DEFAULT 0,
    duration_seconds INTEGER NOT NULL,
    progress_percentage DECIMAL(5, 2) DEFAULT 0,
    completed BOOLEAN DEFAULT false,
    last_watched_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, content_id, episode_id)
);

-- Stream events table (for analytics)
CREATE TABLE stream_events (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES playback_sessions(id),
    event_type VARCHAR(50) NOT NULL,
    event_data JSONB,
    position_seconds INTEGER,
    bitrate INTEGER,
    buffer_health DECIMAL(5, 2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Content availability table
CREATE TABLE content_availability (
    id BIGSERIAL PRIMARY KEY,
    content_id BIGINT NOT NULL,
    episode_id BIGINT,
    region VARCHAR(10) NOT NULL,
    available_from TIMESTAMP WITH TIME ZONE,
    available_until TIMESTAMP WITH TIME ZONE,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(content_id, episode_id, region)
);

-- Video quality profiles
CREATE TABLE quality_profiles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    resolution VARCHAR(20) NOT NULL,
    bitrate_video INTEGER NOT NULL,
    bitrate_audio INTEGER NOT NULL,
    codec_video VARCHAR(50) NOT NULL,
    codec_audio VARCHAR(50) NOT NULL,
    container VARCHAR(20) NOT NULL,
    min_subscription_tier VARCHAR(20) NOT NULL,
    is_default BOOLEAN DEFAULT false,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default quality profiles
INSERT INTO quality_profiles (name, resolution, bitrate_video, bitrate_audio, codec_video, codec_audio, container, min_subscription_tier, is_default, display_order)
VALUES
    ('AUTO', 'auto', 0, 0, 'auto', 'auto', 'auto', 'BASIC', true, 0),
    ('SD', '640x480', 1500, 128, 'h264', 'aac', 'fmp4', 'BASIC', false, 1),
    ('HD', '1280x720', 3000, 192, 'h264', 'aac', 'fmp4', 'BASIC', false, 2),
    ('FHD', '1920x1080', 6000, 256, 'h264', 'aac', 'fmp4', 'STANDARD', false, 3),
    ('4K', '3840x2160', 15000, 384, 'h265', 'eac3', 'fmp4', 'PREMIUM', false, 4),
    ('4K_HDR', '3840x2160', 20000, 384, 'h265', 'eac3', 'fmp4', 'PREMIUM', false, 5);

-- Indexes
CREATE INDEX idx_playback_sessions_user_id ON playback_sessions(user_id);
CREATE INDEX idx_playback_sessions_content_id ON playback_sessions(content_id);
CREATE INDEX idx_playback_sessions_active ON playback_sessions(user_id, is_active) WHERE is_active = true;
CREATE INDEX idx_playback_sessions_last_heartbeat ON playback_sessions(last_heartbeat);

CREATE INDEX idx_watch_progress_user_id ON watch_progress(user_id);
CREATE INDEX idx_watch_progress_content ON watch_progress(user_id, content_id);
CREATE INDEX idx_watch_progress_last_watched ON watch_progress(user_id, last_watched_at DESC);

CREATE INDEX idx_stream_events_session ON stream_events(session_id);
CREATE INDEX idx_stream_events_created ON stream_events(created_at);

CREATE INDEX idx_content_availability_content ON content_availability(content_id, region);
CREATE INDEX idx_content_availability_available ON content_availability(is_available, available_from, available_until);

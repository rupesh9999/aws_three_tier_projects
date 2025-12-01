-- Profile Service Schema
-- V1__init_profile_schema.sql

-- User Profiles table (multiple profiles per account)
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    is_kids_profile BOOLEAN DEFAULT FALSE,
    maturity_level VARCHAR(20) DEFAULT 'ALL',
    language VARCHAR(10) DEFAULT 'en',
    autoplay_enabled BOOLEAN DEFAULT TRUE,
    autoplay_previews BOOLEAN DEFAULT TRUE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_maturity_level CHECK (maturity_level IN ('ALL', 'PG', 'PG13', 'R', 'NC17'))
);

-- Profile preferences
CREATE TABLE profile_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    subtitle_language VARCHAR(10),
    subtitle_enabled BOOLEAN DEFAULT FALSE,
    audio_language VARCHAR(10) DEFAULT 'en',
    playback_quality VARCHAR(20) DEFAULT 'AUTO',
    data_saver_enabled BOOLEAN DEFAULT FALSE,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    email_notifications BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_playback_quality CHECK (playback_quality IN ('AUTO', 'LOW', 'MEDIUM', 'HIGH', 'ULTRA'))
);

-- Profile genre preferences (for recommendations)
CREATE TABLE profile_genre_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    genre_id UUID NOT NULL,
    preference_weight DECIMAL(3,2) DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Profile content ratings (for recommendations ML)
CREATE TABLE profile_content_ratings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    content_id UUID NOT NULL,
    rating DECIMAL(2,1),
    thumbs_up BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rating CHECK (rating >= 0 AND rating <= 5)
);

-- Profile watchlist (My List)
CREATE TABLE profile_watchlist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    content_id UUID NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    position INTEGER DEFAULT 0,
    UNIQUE(profile_id, content_id)
);

-- Profile PIN for parental controls
CREATE TABLE profile_pins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
    pin_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(profile_id)
);

-- Indexes
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_user_default ON user_profiles(user_id, is_default);
CREATE INDEX idx_profile_preferences_profile_id ON profile_preferences(profile_id);
CREATE INDEX idx_profile_genre_pref_profile_id ON profile_genre_preferences(profile_id);
CREATE INDEX idx_profile_content_ratings_profile_id ON profile_content_ratings(profile_id);
CREATE INDEX idx_profile_content_ratings_content_id ON profile_content_ratings(content_id);
CREATE INDEX idx_profile_watchlist_profile_id ON profile_watchlist(profile_id);
CREATE INDEX idx_profile_watchlist_content_id ON profile_watchlist(content_id);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profile_preferences_updated_at BEFORE UPDATE ON profile_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profile_content_ratings_updated_at BEFORE UPDATE ON profile_content_ratings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profile_pins_updated_at BEFORE UPDATE ON profile_pins
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- V1__init_content_schema.sql
-- Initial schema for content service

CREATE SCHEMA IF NOT EXISTS content;

-- Content types enum
CREATE TYPE content.content_type AS ENUM ('MOVIE', 'SERIES', 'DOCUMENTARY', 'SPECIAL');
CREATE TYPE content.content_status AS ENUM ('DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'ARCHIVED', 'DELETED');
CREATE TYPE content.maturity_rating AS ENUM ('G', 'PG', 'PG-13', 'R', 'NC-17', 'TV-Y', 'TV-Y7', 'TV-G', 'TV-PG', 'TV-14', 'TV-MA');

-- Genres table
CREATE TABLE content.genres (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Content table (base for movies and series)
CREATE TABLE content.contents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type content.content_type NOT NULL,
    title VARCHAR(500) NOT NULL,
    original_title VARCHAR(500),
    slug VARCHAR(500) NOT NULL UNIQUE,
    tagline VARCHAR(500),
    synopsis TEXT,
    description TEXT,
    release_date DATE,
    release_year INTEGER,
    runtime_minutes INTEGER,
    maturity_rating content.maturity_rating DEFAULT 'TV-MA',
    status content.content_status DEFAULT 'DRAFT',
    
    -- Media URLs
    poster_url VARCHAR(1000),
    backdrop_url VARCHAR(1000),
    trailer_url VARCHAR(1000),
    logo_url VARCHAR(1000),
    
    -- Metadata
    original_language VARCHAR(10) DEFAULT 'en',
    countries VARCHAR(500)[],
    keywords VARCHAR(100)[],
    
    -- Ratings
    imdb_id VARCHAR(20),
    imdb_rating DECIMAL(3,1),
    tmdb_id INTEGER,
    tmdb_rating DECIMAL(3,1),
    internal_rating DECIMAL(3,1),
    
    -- Stats
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    
    -- Featured/Trending
    is_featured BOOLEAN DEFAULT FALSE,
    featured_until TIMESTAMP,
    is_trending BOOLEAN DEFAULT FALSE,
    trending_score DECIMAL(10,2) DEFAULT 0,
    
    -- Series specific
    total_seasons INTEGER,
    total_episodes INTEGER,
    
    -- Timestamps
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    
    -- Soft delete
    deleted_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Content-Genre junction table
CREATE TABLE content.content_genres (
    content_id UUID NOT NULL REFERENCES content.contents(id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES content.genres(id) ON DELETE CASCADE,
    display_order INTEGER DEFAULT 0,
    PRIMARY KEY (content_id, genre_id)
);

-- Seasons table (for series)
CREATE TABLE content.seasons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id UUID NOT NULL REFERENCES content.contents(id) ON DELETE CASCADE,
    season_number INTEGER NOT NULL,
    title VARCHAR(500),
    synopsis TEXT,
    poster_url VARCHAR(1000),
    air_date DATE,
    episode_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(content_id, season_number)
);

-- Episodes table
CREATE TABLE content.episodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id UUID NOT NULL REFERENCES content.seasons(id) ON DELETE CASCADE,
    content_id UUID NOT NULL REFERENCES content.contents(id) ON DELETE CASCADE,
    episode_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    synopsis TEXT,
    runtime_minutes INTEGER,
    air_date DATE,
    thumbnail_url VARCHAR(1000),
    still_url VARCHAR(1000),
    
    -- Video details
    video_url VARCHAR(1000),
    video_key VARCHAR(500),
    
    -- Stats
    view_count BIGINT DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(season_id, episode_number)
);

-- Cast and Crew
CREATE TABLE content.people (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    biography TEXT,
    birth_date DATE,
    death_date DATE,
    birthplace VARCHAR(255),
    profile_url VARCHAR(1000),
    imdb_id VARCHAR(20),
    tmdb_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE content.content_cast (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id UUID NOT NULL REFERENCES content.contents(id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES content.people(id) ON DELETE CASCADE,
    character_name VARCHAR(255),
    role_type VARCHAR(50) DEFAULT 'ACTOR',
    display_order INTEGER DEFAULT 0,
    UNIQUE(content_id, person_id, character_name)
);

CREATE TABLE content.content_crew (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id UUID NOT NULL REFERENCES content.contents(id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES content.people(id) ON DELETE CASCADE,
    job VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    display_order INTEGER DEFAULT 0,
    UNIQUE(content_id, person_id, job)
);

-- Video assets (multiple quality versions)
CREATE TABLE content.video_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id UUID REFERENCES content.contents(id) ON DELETE CASCADE,
    episode_id UUID REFERENCES content.episodes(id) ON DELETE CASCADE,
    
    quality VARCHAR(20) NOT NULL,  -- SD, HD, FHD, UHD
    video_codec VARCHAR(50),
    audio_codec VARCHAR(50),
    container_format VARCHAR(20),
    
    file_url VARCHAR(1000) NOT NULL,
    file_key VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT,
    duration_seconds INTEGER,
    bitrate_kbps INTEGER,
    resolution_width INTEGER,
    resolution_height INTEGER,
    
    is_hdr BOOLEAN DEFAULT FALSE,
    is_dolby_vision BOOLEAN DEFAULT FALSE,
    has_dolby_atmos BOOLEAN DEFAULT FALSE,
    
    status VARCHAR(50) DEFAULT 'PROCESSING',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (content_id IS NOT NULL OR episode_id IS NOT NULL)
);

-- Subtitles
CREATE TABLE content.subtitles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id UUID REFERENCES content.contents(id) ON DELETE CASCADE,
    episode_id UUID REFERENCES content.episodes(id) ON DELETE CASCADE,
    
    language_code VARCHAR(10) NOT NULL,
    language_name VARCHAR(100) NOT NULL,
    type VARCHAR(20) DEFAULT 'SRT',  -- SRT, VTT, ASS
    
    file_url VARCHAR(1000) NOT NULL,
    file_key VARCHAR(500) NOT NULL,
    
    is_default BOOLEAN DEFAULT FALSE,
    is_forced BOOLEAN DEFAULT FALSE,
    is_closed_caption BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (content_id IS NOT NULL OR episode_id IS NOT NULL)
);

-- Audio tracks
CREATE TABLE content.audio_tracks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_asset_id UUID NOT NULL REFERENCES content.video_assets(id) ON DELETE CASCADE,
    
    language_code VARCHAR(10) NOT NULL,
    language_name VARCHAR(100) NOT NULL,
    
    codec VARCHAR(50),
    channels INTEGER DEFAULT 2,
    bitrate_kbps INTEGER,
    
    is_default BOOLEAN DEFAULT FALSE,
    is_original BOOLEAN DEFAULT FALSE,
    is_commentary BOOLEAN DEFAULT FALSE,
    is_descriptive BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Content availability (regional restrictions)
CREATE TABLE content.content_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id UUID NOT NULL REFERENCES content.contents(id) ON DELETE CASCADE,
    
    country_code VARCHAR(10) NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    available_from TIMESTAMP,
    available_until TIMESTAMP,
    
    subscription_required VARCHAR(50)[],
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(content_id, country_code)
);

-- Insert default genres
INSERT INTO content.genres (name, slug, display_order) VALUES
    ('Action', 'action', 1),
    ('Adventure', 'adventure', 2),
    ('Animation', 'animation', 3),
    ('Comedy', 'comedy', 4),
    ('Crime', 'crime', 5),
    ('Documentary', 'documentary', 6),
    ('Drama', 'drama', 7),
    ('Family', 'family', 8),
    ('Fantasy', 'fantasy', 9),
    ('History', 'history', 10),
    ('Horror', 'horror', 11),
    ('Music', 'music', 12),
    ('Mystery', 'mystery', 13),
    ('Romance', 'romance', 14),
    ('Science Fiction', 'science-fiction', 15),
    ('Thriller', 'thriller', 16),
    ('War', 'war', 17),
    ('Western', 'western', 18),
    ('Reality', 'reality', 19),
    ('Talk Show', 'talk-show', 20);

-- Create indexes
CREATE INDEX idx_contents_type ON content.contents(type);
CREATE INDEX idx_contents_status ON content.contents(status);
CREATE INDEX idx_contents_slug ON content.contents(slug);
CREATE INDEX idx_contents_release ON content.contents(release_year, release_date);
CREATE INDEX idx_contents_rating ON content.contents(maturity_rating);
CREATE INDEX idx_contents_trending ON content.contents(trending_score DESC) WHERE is_trending = TRUE;
CREATE INDEX idx_contents_featured ON content.contents(featured_until) WHERE is_featured = TRUE;
CREATE INDEX idx_contents_search ON content.contents USING gin(to_tsvector('english', title || ' ' || COALESCE(synopsis, '')));

CREATE INDEX idx_seasons_content ON content.seasons(content_id);
CREATE INDEX idx_episodes_season ON content.episodes(season_id);
CREATE INDEX idx_episodes_content ON content.episodes(content_id);

CREATE INDEX idx_content_genres_content ON content.content_genres(content_id);
CREATE INDEX idx_content_genres_genre ON content.content_genres(genre_id);

CREATE INDEX idx_video_assets_content ON content.video_assets(content_id);
CREATE INDEX idx_video_assets_episode ON content.video_assets(episode_id);

CREATE INDEX idx_people_slug ON content.people(slug);
CREATE INDEX idx_content_cast_content ON content.content_cast(content_id);
CREATE INDEX idx_content_cast_person ON content.content_cast(person_id);

-- Update trigger
CREATE OR REPLACE FUNCTION content.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_contents_updated_at BEFORE UPDATE ON content.contents
    FOR EACH ROW EXECUTE FUNCTION content.update_updated_at_column();

CREATE TRIGGER update_seasons_updated_at BEFORE UPDATE ON content.seasons
    FOR EACH ROW EXECUTE FUNCTION content.update_updated_at_column();

CREATE TRIGGER update_episodes_updated_at BEFORE UPDATE ON content.episodes
    FOR EACH ROW EXECUTE FUNCTION content.update_updated_at_column();

CREATE TRIGGER update_genres_updated_at BEFORE UPDATE ON content.genres
    FOR EACH ROW EXECUTE FUNCTION content.update_updated_at_column();

CREATE TRIGGER update_people_updated_at BEFORE UPDATE ON content.people
    FOR EACH ROW EXECUTE FUNCTION content.update_updated_at_column();

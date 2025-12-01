package com.streamflix.common.constants;

/**
 * Application-wide constants
 */
public final class AppConstants {
    
    private AppConstants() {
        // Prevent instantiation
    }
    
    // Pagination defaults
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // JWT constants
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_HEADER_NAME = "Authorization";
    public static final String JWT_REFRESH_HEADER = "X-Refresh-Token";
    
    // User roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_PREMIUM = "ROLE_PREMIUM";
    public static final String ROLE_CONTENT_MANAGER = "ROLE_CONTENT_MANAGER";
    
    // Subscription plans
    public static final String PLAN_BASIC = "BASIC";
    public static final String PLAN_STANDARD = "STANDARD";
    public static final String PLAN_PREMIUM = "PREMIUM";
    
    // Content types
    public static final String CONTENT_MOVIE = "MOVIE";
    public static final String CONTENT_SERIES = "SERIES";
    public static final String CONTENT_EPISODE = "EPISODE";
    public static final String CONTENT_DOCUMENTARY = "DOCUMENTARY";
    
    // Video quality levels
    public static final String QUALITY_SD = "SD";      // 480p
    public static final String QUALITY_HD = "HD";      // 720p
    public static final String QUALITY_FHD = "FHD";    // 1080p
    public static final String QUALITY_UHD = "UHD";    // 4K
    
    // Maturity ratings
    public static final String RATING_G = "G";
    public static final String RATING_PG = "PG";
    public static final String RATING_PG13 = "PG-13";
    public static final String RATING_R = "R";
    public static final String RATING_NC17 = "NC-17";
    public static final String RATING_TV_Y = "TV-Y";
    public static final String RATING_TV_Y7 = "TV-Y7";
    public static final String RATING_TV_G = "TV-G";
    public static final String RATING_TV_PG = "TV-PG";
    public static final String RATING_TV_14 = "TV-14";
    public static final String RATING_TV_MA = "TV-MA";
    
    // Cache names
    public static final String CACHE_CONTENT = "content";
    public static final String CACHE_USER_PROFILES = "userProfiles";
    public static final String CACHE_RECOMMENDATIONS = "recommendations";
    public static final String CACHE_TRENDING = "trending";
    public static final String CACHE_SEARCH = "search";
    
    // Cache TTL (in seconds)
    public static final long CACHE_TTL_SHORT = 300;       // 5 minutes
    public static final long CACHE_TTL_MEDIUM = 3600;     // 1 hour
    public static final long CACHE_TTL_LONG = 86400;      // 24 hours
    
    // Queue names
    public static final String QUEUE_TRANSCODING = "streamflix-transcoding-queue";
    public static final String QUEUE_NOTIFICATIONS = "streamflix-notifications-queue";
    public static final String QUEUE_ANALYTICS = "streamflix-analytics-queue";
    public static final String QUEUE_EMAIL = "streamflix-email-queue";
    
    // S3 prefixes
    public static final String S3_PREFIX_VIDEOS = "videos/";
    public static final String S3_PREFIX_THUMBNAILS = "thumbnails/";
    public static final String S3_PREFIX_SUBTITLES = "subtitles/";
    public static final String S3_PREFIX_PROFILES = "profiles/";
    public static final String S3_PREFIX_POSTERS = "posters/";
    
    // Playback constants
    public static final int PLAYBACK_RESUME_THRESHOLD_PERCENT = 95;
    public static final int PLAYBACK_MARK_WATCHED_THRESHOLD_PERCENT = 90;
    public static final int MAX_CONCURRENT_STREAMS_BASIC = 1;
    public static final int MAX_CONCURRENT_STREAMS_STANDARD = 2;
    public static final int MAX_CONCURRENT_STREAMS_PREMIUM = 4;
    
    // Profile constants
    public static final int MAX_PROFILES_PER_ACCOUNT = 5;
    public static final int MAX_MY_LIST_ITEMS = 500;
    public static final int MAX_DOWNLOAD_ITEMS = 25;
    
    // Search constants
    public static final int SEARCH_MIN_QUERY_LENGTH = 2;
    public static final int SEARCH_MAX_RESULTS = 50;
    
    // API versioning
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
}

package com.streamflix.content.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "contents", schema = "content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(name = "original_title", length = 500)
    private String originalTitle;
    
    @Column(nullable = false, unique = true, length = 500)
    private String slug;
    
    @Column(length = 500)
    private String tagline;
    
    @Column(columnDefinition = "TEXT")
    private String synopsis;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    @Column(name = "release_year")
    private Integer releaseYear;
    
    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "maturity_rating")
    @Builder.Default
    private MaturityRating maturityRating = MaturityRating.TV_MA;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;
    
    // Media URLs
    @Column(name = "poster_url", length = 1000)
    private String posterUrl;
    
    @Column(name = "backdrop_url", length = 1000)
    private String backdropUrl;
    
    @Column(name = "trailer_url", length = 1000)
    private String trailerUrl;
    
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;
    
    // Metadata
    @Column(name = "original_language", length = 10)
    @Builder.Default
    private String originalLanguage = "en";
    
    @Column(name = "imdb_id", length = 20)
    private String imdbId;
    
    @Column(name = "imdb_rating", precision = 3, scale = 1)
    private BigDecimal imdbRating;
    
    @Column(name = "tmdb_id")
    private Integer tmdbId;
    
    @Column(name = "tmdb_rating", precision = 3, scale = 1)
    private BigDecimal tmdbRating;
    
    @Column(name = "internal_rating", precision = 3, scale = 1)
    private BigDecimal internalRating;
    
    // Stats
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;
    
    @Column(name = "like_count")
    @Builder.Default
    private Long likeCount = 0L;
    
    // Featured/Trending
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;
    
    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;
    
    @Column(name = "is_trending")
    @Builder.Default
    private Boolean isTrending = false;
    
    @Column(name = "trending_score", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal trendingScore = BigDecimal.ZERO;
    
    // Series specific
    @Column(name = "total_seasons")
    private Integer totalSeasons;
    
    @Column(name = "total_episodes")
    private Integer totalEpisodes;
    
    // Timestamps
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private UUID createdBy;
    
    @Column(name = "updated_by")
    private UUID updatedBy;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Version
    private Long version;
    
    // Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "content_genres",
        schema = "content",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seasonNumber ASC")
    @Builder.Default
    private List<Season> seasons = new ArrayList<>();
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContentCast> cast = new ArrayList<>();
    
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContentCrew> crew = new ArrayList<>();
    
    // Helper methods
    public void addGenre(Genre genre) {
        genres.add(genre);
    }
    
    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }
    
    public void addSeason(Season season) {
        seasons.add(season);
        season.setContent(this);
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public boolean isPublished() {
        return status == ContentStatus.PUBLISHED;
    }
    
    public boolean isMovie() {
        return type == ContentType.MOVIE;
    }
    
    public boolean isSeries() {
        return type == ContentType.SERIES;
    }
    
    public enum ContentType {
        MOVIE, SERIES, DOCUMENTARY, SPECIAL
    }
    
    public enum ContentStatus {
        DRAFT, PENDING_REVIEW, PUBLISHED, ARCHIVED, DELETED
    }
    
    public enum MaturityRating {
        G, PG, PG_13, R, NC_17, TV_Y, TV_Y7, TV_G, TV_PG, TV_14, TV_MA
    }
}

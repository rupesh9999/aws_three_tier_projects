package com.streamflix.content.repository;

import com.streamflix.content.entity.Content;
import com.streamflix.content.entity.Content.ContentType;
import com.streamflix.content.entity.Content.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentRepository extends JpaRepository<Content, UUID>, JpaSpecificationExecutor<Content> {
    
    Optional<Content> findBySlug(String slug);
    
    Optional<Content> findByIdAndDeletedAtIsNull(UUID id);
    
    Optional<Content> findBySlugAndDeletedAtIsNull(String slug);
    
    @Query("SELECT c FROM Content c LEFT JOIN FETCH c.genres WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Content> findByIdWithGenres(@Param("id") UUID id);
    
    @Query("SELECT c FROM Content c LEFT JOIN FETCH c.genres WHERE c.slug = :slug AND c.deletedAt IS NULL")
    Optional<Content> findBySlugWithGenres(@Param("slug") String slug);
    
    @Query("SELECT c FROM Content c LEFT JOIN FETCH c.genres LEFT JOIN FETCH c.cast LEFT JOIN FETCH c.crew WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Content> findByIdWithAllRelations(@Param("id") UUID id);
    
    Page<Content> findByTypeAndStatusAndDeletedAtIsNull(ContentType type, ContentStatus status, Pageable pageable);
    
    Page<Content> findByStatusAndDeletedAtIsNull(ContentStatus status, Pageable pageable);
    
    @Query("SELECT c FROM Content c JOIN c.genres g WHERE g.slug = :genreSlug AND c.status = 'PUBLISHED' AND c.deletedAt IS NULL")
    Page<Content> findByGenreSlug(@Param("genreSlug") String genreSlug, Pageable pageable);
    
    @Query("SELECT c FROM Content c WHERE c.isFeatured = true AND c.status = 'PUBLISHED' AND c.deletedAt IS NULL AND (c.featuredUntil IS NULL OR c.featuredUntil > :now)")
    List<Content> findFeatured(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Content c WHERE c.isTrending = true AND c.status = 'PUBLISHED' AND c.deletedAt IS NULL ORDER BY c.trendingScore DESC")
    Page<Content> findTrending(Pageable pageable);
    
    @Query("SELECT c FROM Content c WHERE c.status = 'PUBLISHED' AND c.deletedAt IS NULL ORDER BY c.publishedAt DESC")
    Page<Content> findNewReleases(Pageable pageable);
    
    @Query("SELECT c FROM Content c WHERE c.status = 'PUBLISHED' AND c.deletedAt IS NULL ORDER BY c.viewCount DESC")
    Page<Content> findPopular(Pageable pageable);
    
    @Query("SELECT c FROM Content c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) AND c.status = 'PUBLISHED' AND c.deletedAt IS NULL")
    Page<Content> searchByTitle(@Param("query") String query, Pageable pageable);
    
    @Query(value = "SELECT * FROM content.contents c WHERE c.status = 'PUBLISHED' AND c.deleted_at IS NULL " +
            "AND to_tsvector('english', c.title || ' ' || COALESCE(c.synopsis, '')) @@ plainto_tsquery('english', :query)",
            nativeQuery = true)
    Page<Content> fullTextSearch(@Param("query") String query, Pageable pageable);
    
    @Modifying
    @Query("UPDATE Content c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    void incrementViewCount(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE Content c SET c.likeCount = c.likeCount + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") UUID id);
    
    @Modifying
    @Query("UPDATE Content c SET c.likeCount = c.likeCount - 1 WHERE c.id = :id AND c.likeCount > 0")
    void decrementLikeCount(@Param("id") UUID id);
    
    @Query("SELECT COUNT(c) FROM Content c WHERE c.type = :type AND c.status = 'PUBLISHED' AND c.deletedAt IS NULL")
    long countByType(@Param("type") ContentType type);
    
    boolean existsBySlug(String slug);
    
    @Query("SELECT c FROM Content c WHERE c.id IN :ids AND c.deletedAt IS NULL")
    List<Content> findByIdIn(@Param("ids") List<UUID> ids);
}

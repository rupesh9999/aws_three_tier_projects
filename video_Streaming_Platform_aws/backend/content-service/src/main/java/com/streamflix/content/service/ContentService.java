package com.streamflix.content.service;

import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.content.dto.ContentResponse;
import com.streamflix.content.entity.Content;
import com.streamflix.content.entity.Content.ContentStatus;
import com.streamflix.content.entity.Content.ContentType;
import com.streamflix.content.entity.Genre;
import com.streamflix.content.repository.ContentRepository;
import com.streamflix.content.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final ContentRepository contentRepository;
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "content", key = "#id")
    public ContentResponse getContentById(UUID id) {
        log.debug("Fetching content with ID: {}", id);
        Content content = contentRepository.findByIdWithAllRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));
        return ContentResponse.from(content);
    }

    @Transactional(readOnly = true)
    public ContentResponse getContentBySlug(String slug) {
        log.debug("Fetching content with slug: {}", slug);
        Content content = contentRepository.findBySlugWithGenres(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with slug: " + slug));
        return ContentResponse.from(content);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getAllContent(Pageable pageable) {
        log.debug("Fetching all content with pagination");
        return contentRepository.findByStatusAndDeletedAtIsNull(ContentStatus.PUBLISHED, pageable)
                .map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getContentByType(ContentType type, Pageable pageable) {
        log.debug("Fetching content by type: {}", type);
        return contentRepository.findByTypeAndStatusAndDeletedAtIsNull(type, ContentStatus.PUBLISHED, pageable)
                .map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getContentByGenre(String genreSlug, Pageable pageable) {
        log.debug("Fetching content by genre slug: {}", genreSlug);
        return contentRepository.findByGenreSlug(genreSlug, pageable)
                .map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> getFeaturedContent() {
        log.debug("Fetching featured content");
        return contentRepository.findFeatured(LocalDateTime.now()).stream()
                .map(ContentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getTrendingContent(Pageable pageable) {
        log.debug("Fetching trending content");
        return contentRepository.findTrending(pageable)
                .map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getNewReleases(Pageable pageable) {
        log.debug("Fetching new releases");
        return contentRepository.findNewReleases(pageable)
                .map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getPopularContent(Pageable pageable) {
        log.debug("Fetching popular content");
        return contentRepository.findPopular(pageable)
                .map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> searchContent(String query, Pageable pageable) {
        log.debug("Searching content with query: {}", query);
        return contentRepository.searchByTitle(query, pageable)
                .map(ContentResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> fullTextSearch(String query, Pageable pageable) {
        log.debug("Full text search with query: {}", query);
        return contentRepository.fullTextSearch(query, pageable)
                .map(ContentResponse::from);
    }

    @Transactional
    @CacheEvict(value = "content", allEntries = true)
    public ContentResponse createContent(Content content) {
        log.info("Creating new content: {}", content.getTitle());
        
        if (contentRepository.existsBySlug(content.getSlug())) {
            throw new IllegalArgumentException("Content already exists with slug: " + content.getSlug());
        }

        Content savedContent = contentRepository.save(content);
        log.info("Content created with ID: {}", savedContent.getId());
        return ContentResponse.from(savedContent);
    }

    @Transactional
    @CacheEvict(value = "content", key = "#id")
    public ContentResponse updateContent(UUID id, Content contentUpdate) {
        log.info("Updating content with ID: {}", id);
        
        Content content = contentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));

        // Update fields
        if (contentUpdate.getTitle() != null) {
            content.setTitle(contentUpdate.getTitle());
        }
        if (contentUpdate.getSynopsis() != null) {
            content.setSynopsis(contentUpdate.getSynopsis());
        }
        if (contentUpdate.getDescription() != null) {
            content.setDescription(contentUpdate.getDescription());
        }
        if (contentUpdate.getReleaseDate() != null) {
            content.setReleaseDate(contentUpdate.getReleaseDate());
        }
        if (contentUpdate.getRuntimeMinutes() != null) {
            content.setRuntimeMinutes(contentUpdate.getRuntimeMinutes());
        }
        if (contentUpdate.getMaturityRating() != null) {
            content.setMaturityRating(contentUpdate.getMaturityRating());
        }
        if (contentUpdate.getPosterUrl() != null) {
            content.setPosterUrl(contentUpdate.getPosterUrl());
        }
        if (contentUpdate.getBackdropUrl() != null) {
            content.setBackdropUrl(contentUpdate.getBackdropUrl());
        }
        if (contentUpdate.getTrailerUrl() != null) {
            content.setTrailerUrl(contentUpdate.getTrailerUrl());
        }

        Content updatedContent = contentRepository.save(content);
        log.info("Content updated: {}", updatedContent.getId());
        return ContentResponse.from(updatedContent);
    }

    @Transactional
    @CacheEvict(value = "content", key = "#id")
    public ContentResponse updateContentGenres(UUID id, Set<UUID> genreIds) {
        log.info("Updating genres for content with ID: {}", id);
        
        Content content = contentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));

        Set<Genre> genres = new java.util.HashSet<>(genreRepository.findAllById(genreIds));
        content.setGenres(genres);

        Content updatedContent = contentRepository.save(content);
        return ContentResponse.from(updatedContent);
    }

    @Transactional
    @CacheEvict(value = "content", key = "#id")
    public ContentResponse publishContent(UUID id) {
        log.info("Publishing content with ID: {}", id);
        
        Content content = contentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));

        content.setStatus(ContentStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());

        Content publishedContent = contentRepository.save(content);
        log.info("Content published: {}", publishedContent.getId());
        return ContentResponse.from(publishedContent);
    }

    @Transactional
    @CacheEvict(value = "content", key = "#id")
    public ContentResponse archiveContent(UUID id) {
        log.info("Archiving content with ID: {}", id);
        
        Content content = contentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));

        content.setStatus(ContentStatus.ARCHIVED);

        Content archivedContent = contentRepository.save(content);
        log.info("Content archived: {}", archivedContent.getId());
        return ContentResponse.from(archivedContent);
    }

    @Transactional
    @CacheEvict(value = "content", key = "#id")
    public void deleteContent(UUID id) {
        log.info("Soft deleting content with ID: {}", id);
        
        Content content = contentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));

        content.setDeletedAt(LocalDateTime.now());
        content.setStatus(ContentStatus.DELETED);
        contentRepository.save(content);
        log.info("Content soft deleted: {}", id);
    }

    @Transactional
    public void incrementViewCount(UUID id) {
        log.debug("Incrementing view count for content: {}", id);
        contentRepository.incrementViewCount(id);
    }

    @Transactional
    public void incrementLikeCount(UUID id) {
        log.debug("Incrementing like count for content: {}", id);
        contentRepository.incrementLikeCount(id);
    }

    @Transactional
    public void decrementLikeCount(UUID id) {
        log.debug("Decrementing like count for content: {}", id);
        contentRepository.decrementLikeCount(id);
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> getSimilarContent(UUID id, int limit) {
        log.debug("Fetching similar content for ID: {}", id);
        
        Content content = contentRepository.findByIdWithGenres(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content", id.toString()));

        // For now, return content of the same type
        // In a full implementation, this would use ML recommendations
        return contentRepository.findByTypeAndStatusAndDeletedAtIsNull(
                content.getType(), 
                ContentStatus.PUBLISHED, 
                org.springframework.data.domain.PageRequest.of(0, limit)
            ).stream()
            .filter(c -> !c.getId().equals(id))
            .map(ContentResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> getContentByIds(List<UUID> ids) {
        log.debug("Fetching content by IDs: {}", ids);
        return contentRepository.findByIdIn(ids).stream()
                .map(ContentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countContentByType(ContentType type) {
        return contentRepository.countByType(type);
    }
}

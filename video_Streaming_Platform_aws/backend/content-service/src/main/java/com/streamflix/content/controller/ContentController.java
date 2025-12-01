package com.streamflix.content.controller;

import com.streamflix.common.dto.ApiResponse;
import com.streamflix.content.dto.*;
import com.streamflix.content.entity.Content;
import com.streamflix.content.entity.Genre;
import com.streamflix.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "Content", description = "Content management APIs")
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    @Operation(summary = "Create new content", description = "Create a new movie or series")
    public ResponseEntity<ApiResponse<ContentResponse>> createContent(
            @Valid @RequestBody ContentCreateRequest request) {
        Content content = mapToEntity(request);
        ContentResponse response = contentService.createContent(content);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Content created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    @Operation(summary = "Update content", description = "Update existing content by ID")
    public ResponseEntity<ApiResponse<ContentResponse>> updateContent(
            @PathVariable UUID id,
            @Valid @RequestBody ContentCreateRequest request) {
        Content content = mapToEntity(request);
        ContentResponse response = contentService.updateContent(id, content);
        return ResponseEntity.ok(ApiResponse.success(response, "Content updated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get content by ID", description = "Retrieve content details by ID")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentById(@PathVariable UUID id) {
        ContentResponse content = contentService.getContentById(id);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get content by slug", description = "Retrieve content details by slug")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentBySlug(@PathVariable String slug) {
        ContentResponse content = contentService.getContentBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping
    @Operation(summary = "Get all content", description = "Get content with pagination")
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> getAllContent(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContentResponse> result = contentService.getAllContent(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get content by type", description = "Get content filtered by type")
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> getContentByType(
            @PathVariable Content.ContentType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentResponse> result = contentService.getContentByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/search")
    @Operation(summary = "Search content", description = "Search content by title")
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> searchContent(
            @Parameter(description = "Search query") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentResponse> result = contentService.searchContent(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    @Operation(summary = "Publish content", description = "Make content publicly available")
    public ResponseEntity<ApiResponse<ContentResponse>> publishContent(@PathVariable UUID id) {
        ContentResponse response = contentService.publishContent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Content published successfully"));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CONTENT_MANAGER')")
    @Operation(summary = "Archive content", description = "Archive content to make it unavailable")
    public ResponseEntity<ApiResponse<ContentResponse>> archiveContent(@PathVariable UUID id) {
        ContentResponse response = contentService.archiveContent(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Content archived successfully"));
    }

    @PostMapping("/{id}/view")
    @Operation(summary = "Record view", description = "Increment view count for content")
    public ResponseEntity<Void> recordView(@PathVariable UUID id) {
        contentService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending content", description = "Get list of trending content")
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> getTrending(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentResponse> trending = contentService.getTrendingContent(pageable);
        return ResponseEntity.ok(ApiResponse.success(trending));
    }

    @GetMapping("/new-releases")
    @Operation(summary = "Get new releases", description = "Get recently released content")
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> getNewReleases(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentResponse> newReleases = contentService.getNewReleases(pageable);
        return ResponseEntity.ok(ApiResponse.success(newReleases));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular content", description = "Get most viewed content")
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> getPopular(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentResponse> popular = contentService.getPopularContent(pageable);
        return ResponseEntity.ok(ApiResponse.success(popular));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured content", description = "Get featured content")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getFeatured() {
        List<ContentResponse> featured = contentService.getFeaturedContent();
        return ResponseEntity.ok(ApiResponse.success(featured));
    }

    @GetMapping("/genre/{genreSlug}")
    @Operation(summary = "Get content by genre", description = "Get content for a specific genre")
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> getByGenre(
            @PathVariable String genreSlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContentResponse> content = contentService.getContentByGenre(genreSlug, pageable);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "Get similar content", description = "Get content similar to specified content")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getSimilarContent(
            @PathVariable UUID id,
            @Parameter(description = "Number of items") @RequestParam(defaultValue = "10") int limit) {
        List<ContentResponse> similar = contentService.getSimilarContent(id, limit);
        return ResponseEntity.ok(ApiResponse.success(similar));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete content", description = "Soft delete content")
    public ResponseEntity<ApiResponse<Void>> deleteContent(@PathVariable UUID id) {
        contentService.deleteContent(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Content deleted successfully"));
    }

    private Content mapToEntity(ContentCreateRequest request) {
        return Content.builder()
                .title(request.title())
                .slug(generateSlug(request.title()))
                .synopsis(request.synopsis())
                .description(request.description())
                .type(request.type())
                .releaseDate(request.releaseDate())
                .releaseYear(request.releaseYear())
                .runtimeMinutes(request.runtimeMinutes())
                .originalLanguage(request.originalLanguage() != null ? request.originalLanguage() : "en")
                .maturityRating(request.maturityRating())
                .posterUrl(request.posterUrl())
                .backdropUrl(request.backdropUrl())
                .trailerUrl(request.trailerUrl())
                .logoUrl(request.logoUrl())
                .build();
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}

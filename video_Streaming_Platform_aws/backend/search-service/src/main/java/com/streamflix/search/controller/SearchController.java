package com.streamflix.search.controller;

import com.streamflix.search.dto.*;
import com.streamflix.search.service.ContentSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final ContentSearchService searchService;

    @PostMapping
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        log.debug("Search request: {}", request.getQuery());
        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping
    public ResponseEntity<SearchResponse> searchGet(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) String maturityRating,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) Float minRating,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .type(type)
                .genres(genres)
                .maturityRating(maturityRating)
                .minYear(minYear)
                .maxYear(maxYear)
                .minRating(minRating)
                .language(language)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();
        
        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<SearchResultItem>> getTrending(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.getTrending(Math.min(limit, 50)));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<SearchResultItem>> getPopular(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.getPopular(Math.min(limit, 50)));
    }

    @GetMapping("/similar/{contentId}")
    public ResponseEntity<List<SearchResultItem>> getSimilar(
            @PathVariable String contentId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(searchService.getSimilar(contentId, Math.min(limit, 20)));
    }

    // Admin endpoints for indexing
    @PostMapping("/index")
    public ResponseEntity<Void> indexContent(@RequestBody ContentIndexRequest request) {
        log.info("Indexing content: {}", request.getId());
        searchService.indexContent(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/index/{contentId}")
    public ResponseEntity<Void> deleteFromIndex(@PathVariable String contentId) {
        log.info("Deleting content from index: {}", contentId);
        searchService.deleteContent(contentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/index/{contentId}/stats")
    public ResponseEntity<Void> updateStats(
            @PathVariable String contentId,
            @RequestParam(required = false) Long viewCount,
            @RequestParam(required = false) Float averageRating,
            @RequestParam(required = false) Long ratingCount) {
        searchService.updateStats(contentId, viewCount, averageRating, ratingCount);
        return ResponseEntity.ok().build();
    }
}

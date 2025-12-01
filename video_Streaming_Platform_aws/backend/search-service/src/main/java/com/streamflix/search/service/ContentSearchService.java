package com.streamflix.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.streamflix.search.document.ContentDocument;
import com.streamflix.search.dto.*;
import com.streamflix.search.repository.ContentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ContentSearchRepository searchRepository;

    @Value("${search.default-page-size:20}")
    private int defaultPageSize;

    @Value("${search.max-page-size:100}")
    private int maxPageSize;

    @Value("${search.min-score:0.5}")
    private float minScore;

    @Value("${search.highlight.pre-tag:<em>}")
    private String highlightPreTag;

    @Value("${search.highlight.post-tag:</em>}")
    private String highlightPostTag;

    public com.streamflix.search.dto.SearchResponse search(com.streamflix.search.dto.SearchRequest request) {
        log.info("Searching for: {}", request.getQuery());
        long startTime = System.currentTimeMillis();

        try {
            int pageSize = Math.min(
                    request.getSize() != null ? request.getSize() : defaultPageSize,
                    maxPageSize
            );
            int page = request.getPage() != null ? request.getPage() : 0;

            // Build the query
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // Main search query
            if (request.getQuery() != null && !request.getQuery().isBlank()) {
                boolQuery.must(buildMultiMatchQuery(request.getQuery()));
            }

            // Add filters
            boolQuery.filter(QueryBuilders.term(t -> t.field("status").value("PUBLISHED")));

            if (request.getType() != null && !"ALL".equals(request.getType())) {
                boolQuery.filter(QueryBuilders.term(t -> t.field("type").value(request.getType())));
            }

            if (request.getGenres() != null && !request.getGenres().isEmpty()) {
                boolQuery.filter(QueryBuilders.terms(t -> t.field("genres").terms(v -> v.value(
                        request.getGenres().stream().map(FieldValue::of).collect(Collectors.toList())
                ))));
            }

            if (request.getMaturityRating() != null) {
                boolQuery.filter(QueryBuilders.term(t -> t.field("maturityRating").value(request.getMaturityRating())));
            }

            if (request.getMinYear() != null) {
                boolQuery.filter(QueryBuilders.range(r -> r.field("releaseYear").gte(co.elastic.clients.json.JsonData.of(request.getMinYear()))));
            }

            if (request.getMaxYear() != null) {
                boolQuery.filter(QueryBuilders.range(r -> r.field("releaseYear").lte(co.elastic.clients.json.JsonData.of(request.getMaxYear()))));
            }

            if (request.getMinRating() != null) {
                boolQuery.filter(QueryBuilders.range(r -> r.field("averageRating").gte(co.elastic.clients.json.JsonData.of(request.getMinRating()))));
            }

            if (request.getLanguage() != null) {
                boolQuery.filter(QueryBuilders.term(t -> t.field("language").value(request.getLanguage())));
            }

            // Build sort
            String sortField = getSortField(request.getSortBy());
            SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc;

            // Execute search
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("content")
                    .query(Query.of(q -> q.bool(boolQuery.build())))
                    .from(page * pageSize)
                    .size(pageSize)
                    .sort(so -> so.field(f -> f.field(sortField).order(sortOrder)))
                    .highlight(h -> h
                            .preTags(highlightPreTag)
                            .postTags(highlightPostTag)
                            .fields("title", HighlightField.of(hf -> hf))
                            .fields("description", HighlightField.of(hf -> hf.numberOfFragments(2)))
                    )
                    .aggregations("genres", a -> a.terms(t -> t.field("genres").size(20)))
                    .aggregations("types", a -> a.terms(t -> t.field("type").size(10)))
                    .aggregations("maturityRatings", a -> a.terms(t -> t.field("maturityRating").size(10)))
                    .aggregations("years", a -> a.terms(t -> t.field("releaseYear").size(50)))
            );

            SearchResponse<ContentDocument> response = elasticsearchClient.search(searchRequest, ContentDocument.class);

            // Map results
            List<SearchResultItem> results = response.hits().hits().stream()
                    .map(this::mapHitToResult)
                    .collect(Collectors.toList());

            // Map facets
            Map<String, List<com.streamflix.search.dto.SearchResponse.FacetBucket>> facets = new HashMap<>();
            
            response.aggregations().forEach((key, agg) -> {
                if (agg.isSterms()) {
                    List<com.streamflix.search.dto.SearchResponse.FacetBucket> buckets = agg.sterms().buckets().array().stream()
                            .map(b -> com.streamflix.search.dto.SearchResponse.FacetBucket.builder()
                                    .key(b.key().stringValue())
                                    .count(b.docCount())
                                    .build())
                            .collect(Collectors.toList());
                    facets.put(key, buckets);
                } else if (agg.isLterms()) {
                    List<com.streamflix.search.dto.SearchResponse.FacetBucket> buckets = agg.lterms().buckets().array().stream()
                            .map(b -> com.streamflix.search.dto.SearchResponse.FacetBucket.builder()
                                    .key(String.valueOf(b.key()))
                                    .count(b.docCount())
                                    .build())
                            .collect(Collectors.toList());
                    facets.put(key, buckets);
                }
            });

            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;
            long took = System.currentTimeMillis() - startTime;

            log.info("Search completed in {}ms, found {} results", took, totalHits);

            return com.streamflix.search.dto.SearchResponse.builder()
                    .results(results)
                    .totalHits(totalHits)
                    .totalPages((int) Math.ceil((double) totalHits / pageSize))
                    .currentPage(page)
                    .pageSize(pageSize)
                    .query(request.getQuery())
                    .took(took)
                    .facets(facets)
                    .build();

        } catch (IOException e) {
            log.error("Search failed", e);
            throw new RuntimeException("Search failed", e);
        }
    }

    @Cacheable(value = "trending", unless = "#result == null || #result.isEmpty()")
    public List<SearchResultItem> getTrending(int limit) {
        log.debug("Getting trending content, limit: {}", limit);
        return searchRepository.findTop10ByStatusOrderByTrendingScoreDesc("PUBLISHED")
                .stream()
                .limit(limit)
                .map(this::mapDocumentToResult)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "popular", unless = "#result == null || #result.isEmpty()")
    public List<SearchResultItem> getPopular(int limit) {
        log.debug("Getting popular content, limit: {}", limit);
        return searchRepository.findTop10ByStatusOrderByPopularityScoreDesc("PUBLISHED")
                .stream()
                .limit(limit)
                .map(this::mapDocumentToResult)
                .collect(Collectors.toList());
    }

    public List<SearchResultItem> getSimilar(String contentId, int limit) {
        log.debug("Getting similar content for: {}", contentId);
        
        return searchRepository.findById(contentId)
                .map(doc -> {
                    if (doc.getGenres() != null && !doc.getGenres().isEmpty()) {
                        return searchRepository.findSimilarByGenre(
                                doc.getGenres().get(0),
                                contentId,
                                PageRequest.of(0, limit)
                        ).stream()
                                .map(this::mapDocumentToResult)
                                .collect(Collectors.toList());
                    }
                    return Collections.<SearchResultItem>emptyList();
                })
                .orElse(Collections.emptyList());
    }

    public void indexContent(ContentIndexRequest request) {
        log.info("Indexing content: {}", request.getId());

        // Build suggestion input
        List<String> suggestionInputs = new ArrayList<>();
        suggestionInputs.add(request.getTitle());
        if (request.getOriginalTitle() != null) {
            suggestionInputs.add(request.getOriginalTitle());
        }
        if (request.getCast() != null) {
            suggestionInputs.addAll(request.getCast().stream().limit(5).collect(Collectors.toList()));
        }

        ContentDocument document = ContentDocument.builder()
                .id(request.getId())
                .title(request.getTitle())
                .originalTitle(request.getOriginalTitle())
                .description(request.getDescription())
                .type(request.getType())
                .status(request.getStatus())
                .genres(request.getGenres())
                .genreNames(request.getGenreNames())
                .tags(request.getTags())
                .cast(request.getCast())
                .directors(request.getDirectors())
                .writers(request.getWriters())
                .maturityRating(request.getMaturityRating())
                .releaseDate(request.getReleaseDate())
                .releaseYear(request.getReleaseYear())
                .runtime(request.getRuntime())
                .language(request.getLanguage())
                .availableLanguages(request.getAvailableLanguages())
                .subtitleLanguages(request.getSubtitleLanguages())
                .country(request.getCountry())
                .averageRating(request.getAverageRating())
                .ratingCount(request.getRatingCount())
                .thumbnailUrl(request.getThumbnailUrl())
                .posterUrl(request.getPosterUrl())
                .backdropUrl(request.getBackdropUrl())
                .trailerUrl(request.getTrailerUrl())
                .featured(request.getFeatured())
                .isOriginal(request.getIsOriginal())
                .totalSeasons(request.getTotalSeasons())
                .totalEpisodes(request.getTotalEpisodes())
                .viewCount(0L)
                .searchCount(0L)
                .popularityScore(0f)
                .trendingScore(0f)
                .indexedAt(Instant.now())
                .suggest(ContentDocument.Completion.builder()
                        .input(suggestionInputs)
                        .weight(1)
                        .build())
                .build();

        searchRepository.save(document);
        log.info("Indexed content: {}", request.getId());
    }

    public void deleteContent(String contentId) {
        log.info("Deleting content from index: {}", contentId);
        searchRepository.deleteById(contentId);
    }

    public void updateStats(String contentId, Long viewCount, Float averageRating, Long ratingCount) {
        searchRepository.findById(contentId).ifPresent(doc -> {
            if (viewCount != null) {
                doc.setViewCount(viewCount);
            }
            if (averageRating != null) {
                doc.setAverageRating(averageRating);
            }
            if (ratingCount != null) {
                doc.setRatingCount(ratingCount);
            }
            
            // Recalculate popularity score
            float popularity = calculatePopularityScore(doc.getViewCount(), doc.getAverageRating(), doc.getRatingCount());
            doc.setPopularityScore(popularity);
            doc.setUpdatedAt(Instant.now());
            
            searchRepository.save(doc);
        });
    }

    private Query buildMultiMatchQuery(String query) {
        return Query.of(q -> q.multiMatch(m -> m
                .query(query)
                .fields("title^3", "originalTitle^2", "description", "cast^2", "directors^2", "genreNames", "tags")
                .type(TextQueryType.BestFields)
                .fuzziness("AUTO")
                .prefixLength(2)
        ));
    }

    private String getSortField(String sortBy) {
        if (sortBy == null) return "_score";
        return switch (sortBy.toLowerCase()) {
            case "popularity" -> "popularityScore";
            case "rating" -> "averageRating";
            case "releasedate" -> "releaseDate";
            case "trending" -> "trendingScore";
            default -> "_score";
        };
    }

    private SearchResultItem mapHitToResult(Hit<ContentDocument> hit) {
        ContentDocument doc = hit.source();
        if (doc == null) return null;

        String highlightedTitle = null;
        String highlightedDescription = null;

        if (hit.highlight() != null) {
            if (hit.highlight().containsKey("title")) {
                highlightedTitle = String.join(" ", hit.highlight().get("title"));
            }
            if (hit.highlight().containsKey("description")) {
                highlightedDescription = String.join("... ", hit.highlight().get("description"));
            }
        }

        return SearchResultItem.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .originalTitle(doc.getOriginalTitle())
                .description(doc.getDescription())
                .type(doc.getType())
                .genres(doc.getGenreNames())
                .cast(doc.getCast())
                .directors(doc.getDirectors())
                .maturityRating(doc.getMaturityRating())
                .releaseDate(doc.getReleaseDate())
                .releaseYear(doc.getReleaseYear())
                .runtime(doc.getRuntime())
                .language(doc.getLanguage())
                .averageRating(doc.getAverageRating())
                .ratingCount(doc.getRatingCount())
                .thumbnailUrl(doc.getThumbnailUrl())
                .posterUrl(doc.getPosterUrl())
                .backdropUrl(doc.getBackdropUrl())
                .featured(doc.getFeatured())
                .isOriginal(doc.getIsOriginal())
                .totalSeasons(doc.getTotalSeasons())
                .totalEpisodes(doc.getTotalEpisodes())
                .score(hit.score() != null ? hit.score().floatValue() : null)
                .highlightedTitle(highlightedTitle)
                .highlightedDescription(highlightedDescription)
                .build();
    }

    private SearchResultItem mapDocumentToResult(ContentDocument doc) {
        return SearchResultItem.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .originalTitle(doc.getOriginalTitle())
                .description(doc.getDescription())
                .type(doc.getType())
                .genres(doc.getGenreNames())
                .cast(doc.getCast())
                .directors(doc.getDirectors())
                .maturityRating(doc.getMaturityRating())
                .releaseDate(doc.getReleaseDate())
                .releaseYear(doc.getReleaseYear())
                .runtime(doc.getRuntime())
                .language(doc.getLanguage())
                .averageRating(doc.getAverageRating())
                .ratingCount(doc.getRatingCount())
                .thumbnailUrl(doc.getThumbnailUrl())
                .posterUrl(doc.getPosterUrl())
                .backdropUrl(doc.getBackdropUrl())
                .featured(doc.getFeatured())
                .isOriginal(doc.getIsOriginal())
                .totalSeasons(doc.getTotalSeasons())
                .totalEpisodes(doc.getTotalEpisodes())
                .build();
    }

    private float calculatePopularityScore(Long viewCount, Float rating, Long ratingCount) {
        float viewScore = viewCount != null ? (float) Math.log10(viewCount + 1) : 0;
        float ratingScore = rating != null ? rating : 0;
        float ratingCountScore = ratingCount != null ? (float) Math.log10(ratingCount + 1) : 0;
        
        return (viewScore * 0.4f) + (ratingScore * 0.4f) + (ratingCountScore * 0.2f);
    }
}

package com.streamflix.search.repository;

import com.streamflix.search.document.ContentDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentSearchRepository extends ElasticsearchRepository<ContentDocument, String> {

    Page<ContentDocument> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<ContentDocument> findByGenresContaining(String genre, Pageable pageable);

    Page<ContentDocument> findByType(String type, Pageable pageable);

    Page<ContentDocument> findByTypeAndStatus(String type, String status, Pageable pageable);

    Page<ContentDocument> findByStatusOrderByPopularityScoreDesc(String status, Pageable pageable);

    Page<ContentDocument> findByStatusOrderByTrendingScoreDesc(String status, Pageable pageable);

    Page<ContentDocument> findByFeaturedTrueAndStatus(String status, Pageable pageable);

    Page<ContentDocument> findByIsOriginalTrueAndStatus(String status, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description\", \"cast^2\", \"directors^2\", \"genreNames\"], \"type\": \"best_fields\", \"fuzziness\": \"AUTO\"}}], \"filter\": [{\"term\": {\"status\": \"PUBLISHED\"}}]}}")
    Page<ContentDocument> searchByQuery(String query, Pageable pageable);

    List<ContentDocument> findTop10ByStatusOrderByTrendingScoreDesc(String status);

    List<ContentDocument> findTop10ByStatusOrderByPopularityScoreDesc(String status);

    @Query("{\"bool\": {\"must\": [{\"term\": {\"genres\": \"?0\"}}, {\"term\": {\"status\": \"PUBLISHED\"}}], \"must_not\": [{\"term\": {\"id\": \"?1\"}}]}}")
    List<ContentDocument> findSimilarByGenre(String genre, String excludeId, Pageable pageable);
}

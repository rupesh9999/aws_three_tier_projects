package com.streamflix.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {
    private List<SearchResultItem> results;
    private long totalHits;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private String query;
    private long took; // Time in milliseconds
    private Map<String, List<FacetBucket>> facets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacetBucket {
        private String key;
        private long count;
    }
}

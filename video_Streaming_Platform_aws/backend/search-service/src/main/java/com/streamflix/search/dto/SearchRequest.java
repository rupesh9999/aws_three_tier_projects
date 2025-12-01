package com.streamflix.search.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {

    @Size(min = 1, max = 500, message = "Query must be between 1 and 500 characters")
    private String query;

    private String type; // MOVIE, SERIES, ALL

    private List<String> genres;

    private String maturityRating;

    private Integer minYear;

    private Integer maxYear;

    private Float minRating;

    private String language;

    private String country;

    private String sortBy; // relevance, popularity, rating, releaseDate, trending

    private String sortOrder; // asc, desc

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;
}

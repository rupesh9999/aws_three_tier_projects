package com.streamflix.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultItem {
    private String id;
    private String title;
    private String originalTitle;
    private String description;
    private String type;
    private List<String> genres;
    private List<String> cast;
    private List<String> directors;
    private String maturityRating;
    private LocalDate releaseDate;
    private Integer releaseYear;
    private Integer runtime;
    private String language;
    private Float averageRating;
    private Long ratingCount;
    private String thumbnailUrl;
    private String posterUrl;
    private String backdropUrl;
    private Boolean featured;
    private Boolean isOriginal;
    private Integer totalSeasons;
    private Integer totalEpisodes;
    private Float score; // Search relevance score
    private String highlightedTitle;
    private String highlightedDescription;
}

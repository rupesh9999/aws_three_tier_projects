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
public class ContentIndexRequest {
    private String id;
    private String title;
    private String originalTitle;
    private String description;
    private String type;
    private String status;
    private List<String> genres;
    private List<String> genreNames;
    private List<String> tags;
    private List<String> cast;
    private List<String> directors;
    private List<String> writers;
    private String maturityRating;
    private LocalDate releaseDate;
    private Integer releaseYear;
    private Integer runtime;
    private String language;
    private List<String> availableLanguages;
    private List<String> subtitleLanguages;
    private String country;
    private Float averageRating;
    private Long ratingCount;
    private String thumbnailUrl;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private Boolean featured;
    private Boolean isOriginal;
    private Integer totalSeasons;
    private Integer totalEpisodes;
}

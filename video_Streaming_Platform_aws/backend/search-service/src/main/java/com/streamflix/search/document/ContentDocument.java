package com.streamflix.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Document(indexName = "content")
@Setting(settingPath = "elasticsearch/content-settings.json")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "content_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "content_analyzer")
    private String originalTitle;

    @Field(type = FieldType.Text, analyzer = "content_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String type; // MOVIE, SERIES

    @Field(type = FieldType.Keyword)
    private String status; // DRAFT, PUBLISHED, ARCHIVED

    @Field(type = FieldType.Keyword)
    private List<String> genres;

    @Field(type = FieldType.Text, analyzer = "content_analyzer")
    private List<String> genreNames;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Text, analyzer = "content_analyzer")
    private List<String> cast;

    @Field(type = FieldType.Text, analyzer = "content_analyzer")
    private List<String> directors;

    @Field(type = FieldType.Text, analyzer = "content_analyzer")
    private List<String> writers;

    @Field(type = FieldType.Keyword)
    private String maturityRating;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate releaseDate;

    @Field(type = FieldType.Integer)
    private Integer releaseYear;

    @Field(type = FieldType.Integer)
    private Integer runtime; // in minutes

    @Field(type = FieldType.Keyword)
    private String language;

    @Field(type = FieldType.Keyword)
    private List<String> availableLanguages;

    @Field(type = FieldType.Keyword)
    private List<String> subtitleLanguages;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Float)
    private Float averageRating;

    @Field(type = FieldType.Long)
    private Long ratingCount;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Long)
    private Long searchCount;

    @Field(type = FieldType.Float)
    private Float popularityScore;

    @Field(type = FieldType.Float)
    private Float trendingScore;

    @Field(type = FieldType.Text)
    private String thumbnailUrl;

    @Field(type = FieldType.Text)
    private String posterUrl;

    @Field(type = FieldType.Text)
    private String backdropUrl;

    @Field(type = FieldType.Text)
    private String trailerUrl;

    @Field(type = FieldType.Boolean)
    private Boolean featured;

    @Field(type = FieldType.Boolean)
    private Boolean isOriginal;

    // Series specific
    @Field(type = FieldType.Integer)
    private Integer totalSeasons;

    @Field(type = FieldType.Integer)
    private Integer totalEpisodes;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant updatedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant indexedAt;

    // For autocomplete suggestions
    @CompletionField(maxInputLength = 100)
    private Completion suggest;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Completion {
        private List<String> input;
        private Integer weight;
        private Map<String, List<String>> contexts;
    }
}

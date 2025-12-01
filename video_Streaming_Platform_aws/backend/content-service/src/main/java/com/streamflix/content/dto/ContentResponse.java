package com.streamflix.content.dto;

import com.streamflix.content.entity.Content;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ContentResponse(
    UUID id,
    String title,
    String description,
    String synopsis,
    Content.ContentType type,
    LocalDate releaseDate,
    Integer releaseYear,
    Integer runtimeMinutes,
    String originalLanguage,
    Content.MaturityRating maturityRating,
    Content.ContentStatus status,
    BigDecimal internalRating,
    Long viewCount,
    String posterUrl,
    String backdropUrl,
    String trailerUrl,
    String logoUrl,
    Set<GenreResponse> genres,
    List<SeasonResponse> seasons,
    List<CastResponse> cast,
    List<CrewResponse> crew,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ContentResponse from(Content content) {
        return new ContentResponse(
            content.getId(),
            content.getTitle(),
            content.getDescription(),
            content.getSynopsis(),
            content.getType(),
            content.getReleaseDate(),
            content.getReleaseYear(),
            content.getRuntimeMinutes(),
            content.getOriginalLanguage(),
            content.getMaturityRating(),
            content.getStatus(),
            content.getInternalRating(),
            content.getViewCount(),
            content.getPosterUrl(),
            content.getBackdropUrl(),
            content.getTrailerUrl(),
            content.getLogoUrl(),
            content.getGenres() != null ? 
                content.getGenres().stream().map(GenreResponse::from).collect(java.util.stream.Collectors.toSet()) : null,
            content.getSeasons() != null ?
                content.getSeasons().stream().map(SeasonResponse::from).toList() : null,
            content.getCast() != null ?
                content.getCast().stream().map(CastResponse::from).toList() : null,
            content.getCrew() != null ?
                content.getCrew().stream().map(CrewResponse::from).toList() : null,
            content.getCreatedAt(),
            content.getUpdatedAt()
        );
    }
}

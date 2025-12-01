package com.streamflix.content.dto;

import com.streamflix.content.entity.Season;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SeasonResponse(
    UUID id,
    Integer seasonNumber,
    String title,
    String synopsis,
    LocalDate airDate,
    String posterUrl,
    Integer episodeCount,
    List<EpisodeResponse> episodes
) {
    public static SeasonResponse from(Season season) {
        return new SeasonResponse(
            season.getId(),
            season.getSeasonNumber(),
            season.getTitle(),
            season.getSynopsis(),
            season.getAirDate(),
            season.getPosterUrl(),
            season.getEpisodeCount(),
            season.getEpisodes() != null ?
                season.getEpisodes().stream().map(EpisodeResponse::from).toList() : null
        );
    }
}

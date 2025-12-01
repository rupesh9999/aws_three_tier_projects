package com.streamflix.content.dto;

import com.streamflix.content.entity.Episode;
import java.time.LocalDate;
import java.util.UUID;

public record EpisodeResponse(
    UUID id,
    Integer episodeNumber,
    String title,
    String synopsis,
    Integer runtimeMinutes,
    LocalDate airDate,
    String thumbnailUrl,
    String stillUrl,
    String videoUrl,
    Long viewCount
) {
    public static EpisodeResponse from(Episode episode) {
        return new EpisodeResponse(
            episode.getId(),
            episode.getEpisodeNumber(),
            episode.getTitle(),
            episode.getSynopsis(),
            episode.getRuntimeMinutes(),
            episode.getAirDate(),
            episode.getThumbnailUrl(),
            episode.getStillUrl(),
            episode.getVideoUrl(),
            episode.getViewCount()
        );
    }
}

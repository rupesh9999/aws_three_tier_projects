package com.streamflix.content.dto;

import com.streamflix.content.entity.Genre;
import java.util.UUID;

public record GenreResponse(
    UUID id,
    String name,
    String slug,
    String description
) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(
            genre.getId(),
            genre.getName(),
            genre.getSlug(),
            genre.getDescription()
        );
    }
}

package com.streamflix.content.dto;

import com.streamflix.content.entity.Content;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ContentCreateRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    String title,

    @Size(max = 5000, message = "Synopsis must be less than 5000 characters")
    String synopsis,
    
    @Size(max = 10000, message = "Description must be less than 10000 characters")
    String description,

    @NotNull(message = "Content type is required")
    Content.ContentType type,

    LocalDate releaseDate,
    
    Integer releaseYear,

    @Min(value = 1, message = "Runtime must be at least 1 minute")
    Integer runtimeMinutes,

    @Size(max = 10, message = "Original language must be less than 10 characters")
    String originalLanguage,

    Content.MaturityRating maturityRating,

    Set<UUID> genreIds,

    String posterUrl,
    String backdropUrl,
    String trailerUrl,
    String logoUrl
) {}

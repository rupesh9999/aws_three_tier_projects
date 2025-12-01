package com.streamflix.content.dto;

import com.streamflix.content.entity.Content;
import java.util.UUID;

public record ContentSearchCriteria(
    String query,
    Content.ContentType type,
    UUID genreId,
    String language,
    Content.MaturityRating maturityRating,
    Integer releaseYearFrom,
    Integer releaseYearTo,
    Double minRating,
    Content.ContentStatus status,
    String sortBy,
    String sortDirection,
    int page,
    int size
) {
    public ContentSearchCriteria {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null) sortBy = "releaseDate";
        if (sortDirection == null) sortDirection = "desc";
    }
}

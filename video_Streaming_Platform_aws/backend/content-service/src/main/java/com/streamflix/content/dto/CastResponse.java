package com.streamflix.content.dto;

import com.streamflix.content.entity.ContentCast;
import java.util.UUID;

public record CastResponse(
    UUID id,
    PersonResponse person,
    String characterName,
    String roleType,
    Integer displayOrder
) {
    public static CastResponse from(ContentCast cast) {
        return new CastResponse(
            cast.getId(),
            PersonResponse.from(cast.getPerson()),
            cast.getCharacterName(),
            cast.getRoleType(),
            cast.getDisplayOrder()
        );
    }
}

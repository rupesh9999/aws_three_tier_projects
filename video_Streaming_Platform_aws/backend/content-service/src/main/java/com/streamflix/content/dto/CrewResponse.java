package com.streamflix.content.dto;

import com.streamflix.content.entity.ContentCrew;
import java.util.UUID;

public record CrewResponse(
    UUID id,
    PersonResponse person,
    String job,
    String department,
    Integer displayOrder
) {
    public static CrewResponse from(ContentCrew crew) {
        return new CrewResponse(
            crew.getId(),
            PersonResponse.from(crew.getPerson()),
            crew.getJob(),
            crew.getDepartment(),
            crew.getDisplayOrder()
        );
    }
}

package com.streamflix.content.dto;

import com.streamflix.content.entity.Person;
import java.time.LocalDate;
import java.util.UUID;

public record PersonResponse(
    UUID id,
    String name,
    String slug,
    String biography,
    LocalDate birthDate,
    LocalDate deathDate,
    String birthplace,
    String profileUrl,
    String imdbId
) {
    public static PersonResponse from(Person person) {
        return new PersonResponse(
            person.getId(),
            person.getName(),
            person.getSlug(),
            person.getBiography(),
            person.getBirthDate(),
            person.getDeathDate(),
            person.getBirthplace(),
            person.getProfileUrl(),
            person.getImdbId()
        );
    }
}

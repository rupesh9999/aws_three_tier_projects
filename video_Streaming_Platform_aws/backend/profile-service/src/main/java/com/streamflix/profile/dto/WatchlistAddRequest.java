package com.streamflix.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchlistAddRequest {

    @NotNull(message = "Content ID is required")
    private UUID contentId;

    private Integer position;
}

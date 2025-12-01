package com.streamflix.content.controller;

import com.streamflix.common.dto.ApiResponse;
import com.streamflix.content.dto.GenreResponse;
import com.streamflix.content.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Tag(name = "Genres", description = "Genre management APIs")
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    @Operation(summary = "Get all genres", description = "Retrieve list of all genres")
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        List<GenreResponse> genres = genreService.getAllGenres();
        return ResponseEntity.ok(ApiResponse.success(genres));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get genre by ID", description = "Retrieve a specific genre by ID")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreById(@PathVariable UUID id) {
        GenreResponse genre = genreService.getGenreById(id);
        return ResponseEntity.ok(ApiResponse.success(genre));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get genre by slug", description = "Retrieve a specific genre by slug")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreBySlug(@PathVariable String slug) {
        GenreResponse genre = genreService.getGenreBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(genre));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create genre", description = "Create a new genre")
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(
            @RequestParam @NotBlank String name,
            @RequestParam(required = false) String description) {
        GenreResponse genre = genreService.createGenre(name, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(genre, "Genre created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update genre", description = "Update an existing genre")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable UUID id,
            @RequestParam @NotBlank String name,
            @RequestParam(required = false) String description) {
        GenreResponse genre = genreService.updateGenre(id, name, description);
        return ResponseEntity.ok(ApiResponse.success(genre, "Genre updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete genre", description = "Delete a genre")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Genre deleted successfully"));
    }
}

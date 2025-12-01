package com.streamflix.content.service;

import com.streamflix.common.exception.ResourceNotFoundException;
import com.streamflix.content.dto.GenreResponse;
import com.streamflix.content.entity.Genre;
import com.streamflix.content.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "genres")
    public List<GenreResponse> getAllGenres() {
        log.debug("Fetching all genres");
        return genreRepository.findAll().stream()
                .map(GenreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "genre", key = "#id")
    public GenreResponse getGenreById(UUID id) {
        log.debug("Fetching genre with ID: {}", id);
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", id.toString()));
        return GenreResponse.from(genre);
    }

    @Transactional(readOnly = true)
    public GenreResponse getGenreBySlug(String slug) {
        log.debug("Fetching genre with slug: {}", slug);
        Genre genre = genreRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with slug: " + slug));
        return GenreResponse.from(genre);
    }

    @Transactional
    @CacheEvict(value = {"genres", "genre"}, allEntries = true)
    public GenreResponse createGenre(String name, String description) {
        log.info("Creating new genre: {}", name);
        
        if (genreRepository.existsByName(name)) {
            throw new IllegalArgumentException("Genre already exists with name: " + name);
        }

        Genre genre = new Genre();
        genre.setName(name);
        genre.setSlug(generateSlug(name));
        genre.setDescription(description);

        Genre savedGenre = genreRepository.save(genre);
        log.info("Genre created with ID: {}", savedGenre.getId());
        return GenreResponse.from(savedGenre);
    }

    @Transactional
    @CacheEvict(value = {"genres", "genre"}, allEntries = true)
    public GenreResponse updateGenre(UUID id, String name, String description) {
        log.info("Updating genre with ID: {}", id);
        
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", id.toString()));

        if (!genre.getName().equals(name) && genreRepository.existsByName(name)) {
            throw new IllegalArgumentException("Genre already exists with name: " + name);
        }

        genre.setName(name);
        genre.setSlug(generateSlug(name));
        genre.setDescription(description);

        Genre updatedGenre = genreRepository.save(genre);
        log.info("Genre updated: {}", updatedGenre.getId());
        return GenreResponse.from(updatedGenre);
    }

    @Transactional
    @CacheEvict(value = {"genres", "genre"}, allEntries = true)
    public void deleteGenre(UUID id) {
        log.info("Deleting genre with ID: {}", id);
        if (!genreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Genre", id.toString());
        }
        genreRepository.deleteById(id);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}

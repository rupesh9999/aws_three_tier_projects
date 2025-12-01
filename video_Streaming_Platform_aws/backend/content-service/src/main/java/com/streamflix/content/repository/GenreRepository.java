package com.streamflix.content.repository;

import com.streamflix.content.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {
    
    Optional<Genre> findBySlug(String slug);
    
    Optional<Genre> findByName(String name);
    
    List<Genre> findByActiveOrderByDisplayOrderAsc(boolean active);
    
    @Query("SELECT g FROM Genre g WHERE g.active = true ORDER BY g.displayOrder ASC")
    List<Genre> findAllActive();
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
}

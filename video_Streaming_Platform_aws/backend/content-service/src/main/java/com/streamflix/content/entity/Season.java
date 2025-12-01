package com.streamflix.content.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "seasons", schema = "content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Season {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;
    
    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;
    
    @Column(length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String synopsis;
    
    @Column(name = "poster_url", length = 1000)
    private String posterUrl;
    
    @Column(name = "air_date")
    private LocalDate airDate;
    
    @Column(name = "episode_count")
    @Builder.Default
    private Integer episodeCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("episodeNumber ASC")
    @Builder.Default
    private List<Episode> episodes = new ArrayList<>();
    
    public void addEpisode(Episode episode) {
        episodes.add(episode);
        episode.setSeason(this);
        episode.setContent(this.content);
        this.episodeCount = episodes.size();
    }
    
    public String getDisplayTitle() {
        return title != null ? title : "Season " + seasonNumber;
    }
}

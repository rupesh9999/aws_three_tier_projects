package com.streamflix.content.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "episodes", schema = "content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Episode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;
    
    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String synopsis;
    
    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;
    
    @Column(name = "air_date")
    private LocalDate airDate;
    
    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;
    
    @Column(name = "still_url", length = 1000)
    private String stillUrl;
    
    @Column(name = "video_url", length = 1000)
    private String videoUrl;
    
    @Column(name = "video_key", length = 500)
    private String videoKey;
    
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public String getDisplayTitle() {
        return "S" + season.getSeasonNumber() + ":E" + episodeNumber + " - " + title;
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
}

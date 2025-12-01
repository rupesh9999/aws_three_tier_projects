package com.streamflix.playback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "watch_progress", schema = "playback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "episode_id")
    private Long episodeId;

    @Column(name = "position_seconds", nullable = false)
    private Integer positionSeconds = 0;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @Column(name = "completed")
    private Boolean completed = false;

    @Column(name = "last_watched_at")
    private Instant lastWatchedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void calculateProgress() {
        if (durationSeconds != null && durationSeconds > 0) {
            this.progressPercentage = BigDecimal.valueOf(
                (double) positionSeconds / durationSeconds * 100
            ).setScale(2, java.math.RoundingMode.HALF_UP);
            this.completed = progressPercentage.compareTo(BigDecimal.valueOf(90)) >= 0;
        }
    }
}

package com.streamflix.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_preferences")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @Column(name = "subtitle_language", length = 10)
    private String subtitleLanguage;

    @Column(name = "subtitle_enabled")
    @Builder.Default
    private Boolean subtitleEnabled = false;

    @Column(name = "audio_language", length = 10)
    @Builder.Default
    private String audioLanguage = "en";

    @Enumerated(EnumType.STRING)
    @Column(name = "playback_quality", length = 20)
    @Builder.Default
    private PlaybackQuality playbackQuality = PlaybackQuality.AUTO;

    @Column(name = "data_saver_enabled")
    @Builder.Default
    private Boolean dataSaverEnabled = false;

    @Column(name = "notifications_enabled")
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "email_notifications")
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "push_notifications")
    @Builder.Default
    private Boolean pushNotifications = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum PlaybackQuality {
        AUTO, LOW, MEDIUM, HIGH, ULTRA
    }
}

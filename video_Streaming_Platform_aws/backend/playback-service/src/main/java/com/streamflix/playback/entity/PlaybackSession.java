package com.streamflix.playback.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "playback_sessions", schema = "playback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaybackSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "episode_id")
    private Long episodeId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(name = "quality")
    private String quality = "AUTO";

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "position_seconds")
    private Integer positionSeconds = 0;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "stream_url")
    private String streamUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum DeviceType {
        WEB, MOBILE_IOS, MOBILE_ANDROID, TV, TABLET, GAME_CONSOLE, OTHER
    }
}

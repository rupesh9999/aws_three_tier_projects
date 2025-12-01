package com.streamflix.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_kids_profile")
    @Builder.Default
    private Boolean isKidsProfile = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "maturity_level", length = 20)
    @Builder.Default
    private MaturityLevel maturityLevel = MaturityLevel.ALL;

    @Column(length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "autoplay_enabled")
    @Builder.Default
    private Boolean autoplayEnabled = true;

    @Column(name = "autoplay_previews")
    @Builder.Default
    private Boolean autoplayPreviews = true;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProfilePreferences preferences;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileGenrePreference> genrePreferences = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProfileWatchlistItem> watchlist = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum MaturityLevel {
        ALL, PG, PG13, R, NC17
    }
}

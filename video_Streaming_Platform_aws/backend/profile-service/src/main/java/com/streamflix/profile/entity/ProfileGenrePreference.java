package com.streamflix.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_genre_preferences")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileGenrePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @Column(name = "genre_id", nullable = false)
    private UUID genreId;

    @Column(name = "preference_weight", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal preferenceWeight = BigDecimal.ONE;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

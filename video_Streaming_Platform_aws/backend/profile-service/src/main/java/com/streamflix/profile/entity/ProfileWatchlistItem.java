package com.streamflix.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_watchlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profile_id", "content_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileWatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column
    @Builder.Default
    private Integer position = 0;

    @CreatedDate
    @Column(name = "added_at", updatable = false)
    private Instant addedAt;
}

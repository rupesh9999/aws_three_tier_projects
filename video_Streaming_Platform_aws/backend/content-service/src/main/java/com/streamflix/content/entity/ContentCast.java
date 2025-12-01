package com.streamflix.content.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "content_cast", schema = "content")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ContentCast {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    
    @Column(name = "character_name", length = 255)
    private String characterName;
    
    @Column(name = "role_type", length = 50)
    @Builder.Default
    private String roleType = "ACTOR";
    
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}

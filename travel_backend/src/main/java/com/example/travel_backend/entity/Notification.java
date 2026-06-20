package com.example.travel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Column(name = "type", nullable = false, length = Integer.MAX_VALUE)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "achievement_id")
    private UUID achievementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @Column(name = "place_suggestion_id")
    private UUID placeSuggestionId;

    @Column(name = "group_key", length = Integer.MAX_VALUE)
    private String groupKey;

    @Column(name = "preview_text", length = Integer.MAX_VALUE)
    private String previewText;

    @Column(name = "reaction_type", length = Integer.MAX_VALUE)
    private String reactionType;

    @ColumnDefault("false")
    @Column(name = "is_read")
    private Boolean isRead;

    @ColumnDefault("false")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;


}
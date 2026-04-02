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
@Table(name = "user_settings")
public class UserSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ColumnDefault("true")
    @Column(name = "push_reactions")
    private Boolean pushReactions = true;

    @ColumnDefault("true")
    @Column(name = "push_comments")
    private Boolean pushComments = true;

    @ColumnDefault("true")
    @Column(name = "push_follows")
    private Boolean pushFollows = true;

    @ColumnDefault("true")
    @Column(name = "push_reposts")
    private Boolean pushReposts = true;

    @ColumnDefault("true")
    @Column(name = "push_mentions")
    private Boolean pushMentions = true;

    @ColumnDefault("true")
    @Column(name = "push_achievements")
    private Boolean pushAchievements = true;

    @ColumnDefault("'vi'")
    @Column(name = "language", length = Integer.MAX_VALUE)
    private String language = "vi";

    @ColumnDefault("'system'")
    @Column(name = "theme", length = Integer.MAX_VALUE)
    private String theme = "system";

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
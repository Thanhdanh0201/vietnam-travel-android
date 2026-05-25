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
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    @ColumnDefault("'text'")
    @Column(name = "post_type", nullable = false, length = Integer.MAX_VALUE)
    private String postType;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "province_id")
    private Province province;

    @ColumnDefault("'public'")
    @Column(name = "visibility", length = Integer.MAX_VALUE)
    private String visibility;

    @ColumnDefault("0")
    @Column(name = "reaction_count")
    private Integer reactionCount;

    @ColumnDefault("0")
    @Column(name = "comment_count")
    private Integer commentCount;

    @ColumnDefault("0")
    @Column(name = "repost_count")
    private Integer repostCount;

    @ColumnDefault("false")
    @Column(name = "is_edited")
    private Boolean isEdited;

    @ColumnDefault("false")
    @Column(name = "is_pinned")
    private Boolean isPinned;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_post_id")
    private Post originalPost;


}
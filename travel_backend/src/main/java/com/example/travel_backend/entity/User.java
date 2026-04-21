package com.example.travel_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", length = Integer.MAX_VALUE)
    private String email;

    @Column(name = "name", length = Integer.MAX_VALUE)
    private String name;

    @ColumnDefault("NULL")
    @Column(name = "avatar_url", length = Integer.MAX_VALUE)
    private String avatarUrl;

    @ColumnDefault("0")
    @Column(name = "total_provinces")
    private Integer totalProvinces = 0;

    @ColumnDefault("0")
    @Column(name = "total_places_visited")
    private Integer totalPlacesVisited = 0;

    @ColumnDefault("'newbie'")
    @Column(name = "explorer_level", length = Integer.MAX_VALUE)
    private String explorerLevel = "newbie";

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "bio", length = Integer.MAX_VALUE)
    private String bio;

    @Column(name = "cover_url", length = Integer.MAX_VALUE)
    private String coverUrl;

    @ColumnDefault("0")
    @Column(name = "follower_count")
    private Integer followerCount = 0;

    @ColumnDefault("0")
    @Column(name = "following_count")
    private Integer followingCount = 0;

    @ColumnDefault("0")
    @Column(name = "post_count")
    private Integer postCount = 0;

    @ColumnDefault("false")
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @ColumnDefault("false")
    @Column(name = "is_private")
    private Boolean isPrivate = false;

    @ColumnDefault("now()")
    @Column(name = "last_active_at")
    private OffsetDateTime lastActiveAt;


}
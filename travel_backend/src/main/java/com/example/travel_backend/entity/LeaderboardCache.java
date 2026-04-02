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
@Table(name = "leaderboard_cache")
public class LeaderboardCache {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ColumnDefault("0")
    @Column(name = "total_provinces", nullable = false)
    private Integer totalProvinces;

    @ColumnDefault("0")
    @Column(name = "total_places_visited")
    private Integer totalPlacesVisited;

    @ColumnDefault("0")
    @Column(name = "total_achievements")
    private Integer totalAchievements;

    @ColumnDefault("false")
    @Column(name = "has_all_63")
    private Boolean hasAll63;

    @ColumnDefault("now()")
    @Column(name = "refreshed_at")
    private OffsetDateTime refreshedAt;


}
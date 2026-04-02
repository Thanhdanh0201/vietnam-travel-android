package com.example.travel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "places_recommend_cached")
public class PlacesRecommendCached {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "feature_type", nullable = false, length = Integer.MAX_VALUE)
    private String featureType;

    @Column(name = "context_hash", nullable = false, length = Integer.MAX_VALUE)
    private String contextHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_response", nullable = false)
    private Map<String, Object> aiResponse;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;


}
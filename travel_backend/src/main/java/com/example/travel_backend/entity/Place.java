package com.example.travel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "places")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "type", nullable = false, length = Integer.MAX_VALUE)
    private String type;

    @Column(name = "rating")
    private Float rating;

    @ColumnDefault("0")
    @Column(name = "review_count")
    private Integer reviewCount;

    @ColumnDefault("0")
    @Column(name = "app_rating")
    private Float appRating;

    @ColumnDefault("0")
    @Column(name = "app_review_count")
    private Integer appReviewCount;

    @ColumnDefault("0")
    @Column(name = "search_count")
    private Long searchCount;

    @Column(name = "image_url", length = Integer.MAX_VALUE)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "opening_hours")
    private Map<String, Object> openingHours;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;


}
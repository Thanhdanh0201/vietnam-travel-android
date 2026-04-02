package com.example.travel_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "weather_cache")
public class WeatherCache {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "temp_max")
    private Float tempMax;

    @Column(name = "temp_min")
    private Float tempMin;

    @Column(name = "rain_mm")
    private Float rainMm;

    @Column(name = "humidity")
    private Integer humidity;

    @Column(name = "condition", length = Integer.MAX_VALUE)
    private String condition;

    @ColumnDefault("now()")
    @Column(name = "fetched_at")
    private OffsetDateTime fetchedAt;


}
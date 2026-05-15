package com.example.travel_backend.repository;

import com.example.travel_backend.entity.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherCacheRepository extends JpaRepository<WeatherCache, UUID> {
    Optional<WeatherCache> findByPlace_IdAndForecastDate(UUID placeId, LocalDate forecastDate);

    List<WeatherCache> findByPlace_IdAndForecastDateBetweenOrderByForecastDateAsc(
            UUID placeId,
            LocalDate startInclusive,
            LocalDate endInclusive
    );
}

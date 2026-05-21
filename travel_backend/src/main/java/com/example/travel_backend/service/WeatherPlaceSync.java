package com.example.travel_backend.service;

import com.example.travel_backend.entity.Place;
import com.example.travel_backend.entity.WeatherCache;
import com.example.travel_backend.repository.WeatherCacheRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherPlaceSync {

    private final WeatherCacheRepository weatherCacheRepository;
    private final RestTemplate restTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncForecast(Place place, int forecastDays) {
        int fd = Math.min(Math.max(forecastDays, 1), 16);
        var uri = UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", place.getLat())
                .queryParam("longitude", place.getLng())
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum,weathercode")
                .queryParam("timezone", "Asia/Ho_Chi_Minh")
                .queryParam("forecast_days", fd)
                .build()
                .toUri();

        JsonNode root = restTemplate.getForObject(uri, JsonNode.class);
        if (root == null || !root.has("daily")) {
            throw new IllegalStateException("Invalid Open-Meteo response");
        }
        JsonNode daily = root.get("daily");
        JsonNode times = daily.get("time");
        JsonNode tMax = daily.get("temperature_2m_max");
        JsonNode tMin = daily.get("temperature_2m_min");
        JsonNode rain = daily.get("precipitation_sum");
        JsonNode codes = daily.get("weathercode");
        if (times == null || !times.isArray() || tMax == null || tMin == null || rain == null || codes == null) {
            throw new IllegalStateException("Open-Meteo daily arrays missing");
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (int i = 0; i < times.size(); i++) {
            LocalDate date = LocalDate.parse(times.get(i).asText());
            double max = tMax.get(i).asDouble();
            double min = tMin.get(i).asDouble();
            double precip = rain.get(i).asDouble();
            int code = codes.get(i).asInt();

            WeatherCache row = weatherCacheRepository
                    .findByPlace_IdAndForecastDate(place.getId(), date)
                    .orElseGet(() -> {
                        WeatherCache w = new WeatherCache();
                        w.setPlace(place);
                        w.setForecastDate(date);
                        return w;
                    });
            row.setTempMax((float) max);
            row.setTempMin((float) min);
            row.setRainMm((float) precip);
            row.setCondition(WeatherService.mapWeatherCode(code));
            row.setFetchedAt(now);
            weatherCacheRepository.save(row);
        }
    }
}

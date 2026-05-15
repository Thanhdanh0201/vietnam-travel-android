package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.WeatherDataResponse;
import com.example.travel_backend.entity.Place;
import com.example.travel_backend.entity.WeatherCache;
import com.example.travel_backend.repository.PlaceRepository;
import com.example.travel_backend.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final PlaceRepository placeRepository;
    private final WeatherCacheRepository weatherCacheRepository;
    private final WeatherPlaceSync weatherPlaceSync;

    public WeatherDataResponse getToday(UUID placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        LocalDate today = LocalDate.now(VN);
        Optional<WeatherCache> cached = weatherCacheRepository.findByPlace_IdAndForecastDate(placeId, today);
        if (cached.isEmpty() || !isFresh(cached.get())) {
            weatherPlaceSync.syncForecast(place, 7);
        }
        return weatherCacheRepository.findByPlace_IdAndForecastDate(placeId, today)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Weather unavailable"));
    }

    public List<WeatherDataResponse> getForecast(UUID placeId, int days) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        int n = Math.min(Math.max(days, 1), 16);
        LocalDate today = LocalDate.now(VN);
        Optional<WeatherCache> todayRow = weatherCacheRepository.findByPlace_IdAndForecastDate(placeId, today);
        if (todayRow.isEmpty() || !isFresh(todayRow.get())) {
            weatherPlaceSync.syncForecast(place, Math.max(7, n));
        }
        LocalDate end = today.plusDays(n - 1L);
        List<WeatherCache> rows = weatherCacheRepository.findByPlace_IdAndForecastDateBetweenOrderByForecastDateAsc(
                placeId, today, end
        );
        List<WeatherDataResponse> out = new ArrayList<>();
        for (WeatherCache row : rows) {
            out.add(toResponse(row));
        }
        return out;
    }

    public void refreshAllPlaces() {
        for (Place place : placeRepository.findAll()) {
            try {
                weatherPlaceSync.syncForecast(place, 7);
            } catch (Exception e) {
                log.warn("Weather crawl failed for place {}: {}", place.getId(), e.getMessage());
            }
        }
    }

    private boolean isFresh(WeatherCache cache) {
        OffsetDateTime fetched = cache.getFetchedAt();
        if (fetched == null) {
            return false;
        }
        return fetched.isAfter(OffsetDateTime.now().minusMinutes(60));
    }

    private WeatherDataResponse toResponse(WeatherCache c) {
        WeatherDataResponse r = new WeatherDataResponse();
        r.setPlace_id(c.getPlace().getId());
        r.setForecast_date(c.getForecastDate().toString());
        r.setTemp_max(c.getTempMax());
        r.setTemp_min(c.getTempMin());
        r.setRain_mm(c.getRainMm());
        r.setHumidity(c.getHumidity());
        r.setCondition(c.getCondition());
        if (c.getFetchedAt() != null) {
            r.setFetched_at(c.getFetchedAt().format(ISO_OFFSET));
        }
        return r;
    }

    /**
     * WMO weather interpretation codes (Open-Meteo) → app condition tokens.
     */
    public static String mapWeatherCode(int code) {
        if (code == 0) {
            return "sunny";
        }
        if (code == 1 || code == 2) {
            return "partly_cloudy";
        }
        if (code == 3 || code == 45 || code == 48) {
            return "cloudy";
        }
        if (code >= 51 && code <= 67) {
            return "rainy";
        }
        if (code >= 71 && code <= 77) {
            return "cloudy";
        }
        if (code >= 80 && code <= 86) {
            return "rainy";
        }
        if (code >= 95 && code <= 99) {
            return "stormy";
        }
        return "partly_cloudy";
    }
}

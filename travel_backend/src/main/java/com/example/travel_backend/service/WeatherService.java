package com.example.travel_backend.service;

import com.example.travel_backend.config.WeatherFeaturedLocations;
import com.example.travel_backend.dto.response.WeatherDataResponse;
import com.example.travel_backend.dto.response.WeatherNearbyResponse;
import com.example.travel_backend.entity.Place;
import com.example.travel_backend.entity.WeatherCache;
import com.example.travel_backend.repository.PlaceRepository;
import com.example.travel_backend.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * Carousel trang chủ — chỉ đọc weather_cache, một request.
     */
    @Transactional(readOnly = true)
    public List<WeatherNearbyResponse> getFeaturedCitiesWeather() {
        List<WeatherNearbyResponse> out = new ArrayList<>();
        for (WeatherFeaturedLocations.Entry entry : WeatherFeaturedLocations.ALL) {
            try {
                out.add(getNearbyFromCache(entry));
            } catch (Exception e) {
                log.warn("Featured weather missing for {}: {}", entry.key(), e.getMessage());
            }
        }
        return out;
    }

    @Transactional(readOnly = true)
    public WeatherNearbyResponse getNearby(double lat, double lng) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinates");
        }
        Place place = placeRepository.findNearestByCoordinates(lat, lng)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No place found"));
        WeatherDataResponse weather = getTodayFromCache(place.getId());
        return toNearbyResponse(place, weather, null, null, null);
    }

    public WeatherDataResponse getToday(UUID placeId) {
        return getTodayFromCache(placeId);
    }

    /**
     * Nạp Open-Meteo cho 5 thành phố carousel nếu chưa có bản ghi forecast_date = hôm nay.
     */
    public void warmFeaturedCitiesCache() {
        LocalDate today = LocalDate.now(VN);
        for (WeatherFeaturedLocations.Entry entry : WeatherFeaturedLocations.ALL) {
            try {
                Place place = placeRepository.findNearestByCoordinates(entry.lat(), entry.lng())
                        .orElse(null);
                if (place == null) {
                    log.warn("No place near featured city {}", entry.key());
                    continue;
                }
                if (!hasCacheForDate(place.getId(), today)) {
                    log.info("Weather sync featured city {} -> place {}", entry.key(), place.getName());
                    weatherPlaceSync.syncForecast(place, 7);
                } else {
                    log.debug("Weather cache hit today for featured city {}", entry.key());
                }
            } catch (Exception e) {
                log.warn("Weather warmup failed for {}: {}", entry.key(), e.getMessage());
            }
        }
    }

    public List<WeatherDataResponse> getForecast(UUID placeId, int days) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        int n = Math.min(Math.max(days, 1), 16);
        LocalDate today = LocalDate.now(VN);
        if (!hasCacheForDate(placeId, today)) {
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
        LocalDate today = LocalDate.now(VN);
        for (Place place : placeRepository.findAll()) {
            try {
                if (hasCacheForDate(place.getId(), today)) {
                    continue;
                }
                weatherPlaceSync.syncForecast(place, 7);
            } catch (Exception e) {
                log.warn("Weather crawl failed for place {}: {}", place.getId(), e.getMessage());
            }
        }
    }

    private boolean hasCacheForDate(UUID placeId, LocalDate date) {
        return weatherCacheRepository.findByPlace_IdAndForecastDate(placeId, date).isPresent();
    }

    private WeatherDataResponse getTodayFromCache(UUID placeId) {
        LocalDate today = LocalDate.now(VN);
        WeatherCache cache = weatherCacheRepository.findByPlace_IdAndForecastDate(placeId, today)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Weather not cached for today. Wait for server warmup or call POST /api/crawl/weather"
                ));
        return toResponse(cache);
    }

    private WeatherNearbyResponse getNearbyFromCache(WeatherFeaturedLocations.Entry entry) {
        Place place = placeRepository.findNearestByCoordinates(entry.lat(), entry.lng())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No place for " + entry.key()));
        WeatherDataResponse weather = getTodayFromCache(place.getId());
        return toNearbyResponse(place, weather, entry.key(), entry.displayName(), entry.subtitle());
    }

    private WeatherNearbyResponse toNearbyResponse(
            Place place,
            WeatherDataResponse weather,
            String cityKey,
            String displayName,
            String subtitle
    ) {
        WeatherNearbyResponse out = new WeatherNearbyResponse();
        out.setCity_key(cityKey);
        out.setPlace_id(place.getId());
        out.setPlace_name(displayName != null ? displayName : place.getName());
        if (subtitle != null) {
            out.setProvince_name(subtitle);
        } else if (place.getProvince() != null) {
            out.setProvince_name(place.getProvince().getName());
        }
        if (place.getCity() != null) {
            out.setCity_name(place.getCity().getName());
        }
        out.setForecast_date(weather.getForecast_date());
        out.setTemp_max(weather.getTemp_max());
        out.setTemp_min(weather.getTemp_min());
        out.setRain_mm(weather.getRain_mm());
        out.setHumidity(weather.getHumidity());
        out.setCondition(weather.getCondition());
        out.setFetched_at(weather.getFetched_at());
        return out;
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

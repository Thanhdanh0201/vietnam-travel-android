package com.example.travel_backend.config;

import com.example.travel_backend.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Mỗi lần chạy backend: nạp weather_cache cho 5 thành phố nổi bật (bỏ qua nếu đã có bản ghi hôm nay).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherCacheStartupRunner implements ApplicationRunner {

    private final WeatherService weatherService;

    @Override
    public void run(ApplicationArguments args) {
        Thread warmup = new Thread(() -> {
            log.info("Weather cache warmup on startup (featured cities, background)");
            try {
                weatherService.warmFeaturedCitiesCache();
                log.info("Weather cache warmup completed");
            } catch (Exception e) {
                log.warn("Weather cache warmup failed: {}", e.getMessage());
            }
        }, "weather-cache-warmup");
        warmup.setDaemon(true);
        warmup.start();
    }
}

package com.example.travel_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherCrawlService {

    private final WeatherService weatherService;

    @Async
    public void crawlAllPlacesAsync() {
        log.info("Starting async weather crawl for all places");
        weatherService.refreshAllPlaces();
        log.info("Finished async weather crawl");
    }

    /** Chạy khi server start — không chặn HTTP. */
    @Async
    public void warmFeaturedCitiesOnStartup() {
        log.info("Weather featured cities warmup (startup)");
        weatherService.warmFeaturedCitiesCache();
        log.info("Weather featured cities warmup finished");
    }
}

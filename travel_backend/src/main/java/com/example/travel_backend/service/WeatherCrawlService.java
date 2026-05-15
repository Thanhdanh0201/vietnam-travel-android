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
}

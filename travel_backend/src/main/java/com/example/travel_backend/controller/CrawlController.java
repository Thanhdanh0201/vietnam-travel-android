package com.example.travel_backend.controller;

import com.example.travel_backend.service.WeatherCrawlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
public class CrawlController {

    private final WeatherCrawlService weatherCrawlService;

    @PostMapping("/weather")
    public ResponseEntity<Map<String, String>> crawlWeather() {
        weatherCrawlService.crawlAllPlacesAsync();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "started"));
    }
}

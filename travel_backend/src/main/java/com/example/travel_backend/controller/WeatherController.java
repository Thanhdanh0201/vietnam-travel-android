package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.WeatherDataResponse;
import com.example.travel_backend.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/{placeId}")
    public WeatherDataResponse getToday(@PathVariable UUID placeId) {
        return weatherService.getToday(placeId);
    }

    @GetMapping("/{placeId}/forecast")
    public List<WeatherDataResponse> getForecast(
            @PathVariable UUID placeId,
            @RequestParam(name = "days", defaultValue = "7") int days
    ) {
        return weatherService.getForecast(placeId, days);
    }
}

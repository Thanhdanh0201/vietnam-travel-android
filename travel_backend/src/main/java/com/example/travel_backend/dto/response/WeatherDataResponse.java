package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class WeatherDataResponse {
    private UUID place_id;
    private String forecast_date;
    private Float temp_max;
    private Float temp_min;
    private Float rain_mm;
    private Integer humidity;
    private String condition;
    private String fetched_at;
}
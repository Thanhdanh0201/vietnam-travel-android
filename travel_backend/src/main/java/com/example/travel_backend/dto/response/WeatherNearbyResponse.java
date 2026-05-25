package com.example.travel_backend.dto.response;

import lombok.Data;

import java.util.UUID;

/**
 * Thời tiết tại điểm gần GPS nhất (toàn bộ bảng places) + nhãn hiển thị.
 */
@Data
public class WeatherNearbyResponse {
    /** Khớp carousel app: hanoi, hcmc, mui_ne, da_lat, da_nang */
    private String city_key;
    private UUID place_id;
    private String place_name;
    private String province_name;
    private String city_name;
    private String forecast_date;
    private Float temp_max;
    private Float temp_min;
    private Float rain_mm;
    private Integer humidity;
    private String condition;
    private String fetched_at;
}

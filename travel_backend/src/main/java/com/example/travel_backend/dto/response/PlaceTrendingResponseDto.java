package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class PlaceTrendingResponseDto {
    private UUID place_id;
    private UUID province_id;
    private Integer rank_position;
    private Long total_searches; // mapped to score
    private PlaceDetailCompactDto places;

    @Data
    public static class PlaceDetailCompactDto {
        private String name;
        private String image_url;
        private String type;
        private Double lat;
        private Double lng;
    }
}

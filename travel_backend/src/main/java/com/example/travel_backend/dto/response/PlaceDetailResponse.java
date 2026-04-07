package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class PlaceDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private Double lat;
    private Double lng;
    private String type;
    private Float rating;
    private Integer review_count;
    private Float app_rating;
    private Integer app_review_count;
    private String image_url;
    private Map<String, Object> opening_hours;
    private ProvinceDto provinces;
    private CityDto cities;
    private List<PhotoDto> photos;
    private List<EventDto> events;
}
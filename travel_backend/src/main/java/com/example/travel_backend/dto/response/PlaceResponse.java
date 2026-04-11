package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class PlaceResponse {
    private UUID id;
    private String name;
    private Double lat;
    private Double lng;
    private String type;
    private Float rating;
    private Integer review_count;
    private String image_url;
    private ProvinceDto provinces;
    private CityDto cities;
}
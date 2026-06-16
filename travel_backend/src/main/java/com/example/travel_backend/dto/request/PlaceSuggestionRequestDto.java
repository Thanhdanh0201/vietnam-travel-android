package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class PlaceSuggestionRequestDto {
    private String name;
    private UUID provinceId;
    private Double lat;
    private Double lng;
    private String type;
    private String description;
    private String imageUrl;
}

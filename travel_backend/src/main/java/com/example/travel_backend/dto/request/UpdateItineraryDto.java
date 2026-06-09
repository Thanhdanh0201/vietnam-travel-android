package com.example.travel_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateItineraryDto {
    private String title;
    private String description;
    @JsonProperty("is_public")
    private Boolean isPublic;
    private String status;
    @JsonProperty("cover_url")
    private String coverUrl;

}

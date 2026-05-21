package com.example.travel_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PlaceReviewDto {
    private String user_name;
    private String user_avatar_url;
    private String review;
    private Short rating;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    private List<String> photo_urls;
}

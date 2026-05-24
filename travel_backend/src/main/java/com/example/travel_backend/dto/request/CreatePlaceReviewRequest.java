package com.example.travel_backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreatePlaceReviewRequest {
    private Short rating;
    private String review;
    private List<String> photo_urls;
}

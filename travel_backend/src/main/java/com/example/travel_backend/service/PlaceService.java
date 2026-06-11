package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.CreatePlaceReviewRequest;
import com.example.travel_backend.dto.response.PlaceDetailResponse;
import com.example.travel_backend.dto.response.PlaceResponse;
import com.example.travel_backend.dto.response.PlaceReviewDto;
import com.example.travel_backend.entity.PlacePhoto;
import com.example.travel_backend.entity.PlaceTrending;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PlaceService {
    List<PlaceResponse> getPlacesByProvince(String provinceCode, String type, int limit);
    PlaceDetailResponse getPlaceDetail(UUID id);
    List<PlaceResponse> getPlaces(String provinceCode, String type, int limit);
    List<com.example.travel_backend.dto.response.PlaceTrendingResponseDto> getTrendingPlaces(int limit);
    List<PlacePhoto> getPlacePhotos(UUID id);
    Map<String, String> logPlaceAction(UUID id, String actionType, UUID userId);

    PlaceReviewDto createReview(UUID placeId, UUID userId, CreatePlaceReviewRequest request);
    List<PlaceResponse> searchPlaces(String query, int limit);
}
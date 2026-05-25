package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.CreatePlaceReviewRequest;
import com.example.travel_backend.dto.response.PlaceDetailResponse;
import com.example.travel_backend.dto.response.PlaceResponse;
import com.example.travel_backend.dto.response.PlaceReviewDto;
import com.example.travel_backend.entity.PlacePhoto;
import com.example.travel_backend.entity.PlaceTrending;
import com.example.travel_backend.service.PlaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/provinces/{code}/places")
    public ResponseEntity<List<PlaceResponse>> getPlacesByProvince(
            @PathVariable String code,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit) {
        List<PlaceResponse> places = placeService.getPlacesByProvince(code, type, limit);
        return ResponseEntity.ok(places);
    }

    @GetMapping("/places")
    public ResponseEntity<List<PlaceResponse>> getPlaces(
            @RequestParam(required = false) String province_code,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(placeService.getPlaces(province_code, type, limit));
    }

    /** Gợi ý trang chủ — top rating; path con của /api/places/** (permitAll). */
    @GetMapping("/places/recommended")
    public ResponseEntity<List<PlaceResponse>> getRecommendedPlaces(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(placeService.getPlaces(null, null, limit));
    }

    @GetMapping("/places/trending")
    public ResponseEntity<List<PlaceTrending>> getTrendingPlaces(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(placeService.getTrendingPlaces(limit));
    }

    @GetMapping("/places/{id}")
    public ResponseEntity<PlaceDetailResponse> getPlaceDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(placeService.getPlaceDetail(id));
    }

    @PostMapping("/places/{id}/reviews")
    public ResponseEntity<PlaceReviewDto> createPlaceReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreatePlaceReviewRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(placeService.createReview(id, userId, request));
    }

    @GetMapping("/places/{id}/photos")
    public ResponseEntity<List<PlacePhoto>> getPlacePhotos(@PathVariable UUID id) {
        return ResponseEntity.ok(placeService.getPlacePhotos(id));
    }

    @PostMapping("/places/{id}/log")
    public ResponseEntity<Map<String, String>> logPlaceAction(
            @PathVariable UUID id,
            @RequestParam String action_type,
            @RequestParam(required = false) UUID user_id) {
        return ResponseEntity.ok(placeService.logPlaceAction(id, action_type, user_id));
    }
}
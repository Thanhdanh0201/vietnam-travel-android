package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.PlaceDetailResponse;
import com.example.travel_backend.dto.response.PlaceResponse;
import com.example.travel_backend.entity.PlacePhoto;
import com.example.travel_backend.entity.PlaceTrending;
import com.example.travel_backend.service.PlaceService;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/places/trending")
    public ResponseEntity<List<PlaceTrending>> getTrendingPlaces(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(placeService.getTrendingPlaces(limit));
    }

    @GetMapping("/places/{id}")
    public ResponseEntity<PlaceDetailResponse> getPlaceDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(placeService.getPlaceDetail(id));
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
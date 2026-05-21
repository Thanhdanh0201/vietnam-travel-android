package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.UpdateItineraryDto;
import com.example.travel_backend.dto.response.ItineraryResponseDto;
import com.example.travel_backend.service.impl.ItineraryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/itineraries")
public class ItineraryController {

    @Autowired
    private ItineraryServiceImpl itineraryService;

    // GET /api/itineraries?user_id=...&is_public=true&limit=20&offset=0
    @GetMapping
    public ResponseEntity<List<ItineraryResponseDto>> getPublicItineraries(
            @RequestParam("user_id") UUID userId,
            @RequestParam(value = "is_public", defaultValue = "true") Boolean isPublic,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        return ResponseEntity.ok(itineraryService.getPublicItineraries(userId, limit, offset));
    }

    // PATCH /api/itineraries?id={itinerary_id}
    @PatchMapping
    public ResponseEntity<Void> updateItinerary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("id") UUID itineraryId,
            @RequestBody UpdateItineraryDto request) {

        UUID requesterId = UUID.fromString(jwt.getSubject());

        itineraryService.updateItineraryStatus(itineraryId, requesterId, request);

        return ResponseEntity.ok().build();
    }
}
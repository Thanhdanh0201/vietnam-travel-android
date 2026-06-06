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

    // POST /api/itineraries
    @PostMapping
    public ResponseEntity<ItineraryResponseDto> createItinerary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody com.example.travel_backend.dto.request.CreateItineraryRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.createItinerary(myId, request));
    }

    // DELETE /api/itineraries
    @DeleteMapping
    public ResponseEntity<Void> deleteItinerary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("id") UUID itineraryId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        itineraryService.deleteItinerary(itineraryId, myId);
        return ResponseEntity.ok().build();
    }

    // GET /api/itineraries/me
    @GetMapping("/me")
    public ResponseEntity<List<ItineraryResponseDto>> getMyItineraries(
            @AuthenticationPrincipal Jwt jwt) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.getMyItineraries(myId));
    }

    // GET /api/itineraries/{id}/items
    @GetMapping("/{id}/items")
    public ResponseEntity<List<com.example.travel_backend.dto.response.ItineraryItemResponseDto>> getItineraryItems(
            @PathVariable("id") UUID itineraryId) {

        return ResponseEntity.ok(itineraryService.getItineraryItems(itineraryId));
    }

    // POST /api/itineraries/{id}/items
    @PostMapping("/{id}/items")
    public ResponseEntity<com.example.travel_backend.dto.response.ItineraryItemResponseDto> addItineraryItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @RequestBody com.example.travel_backend.dto.request.CreateItineraryItemRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(itineraryService.addItineraryItem(itineraryId, myId, request));
    }

    // DELETE /api/itineraries/{id}/items/{itemId}
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> deleteItineraryItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID itineraryId,
            @PathVariable("itemId") UUID itemId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        itineraryService.deleteItineraryItem(itineraryId, itemId, myId);
        return ResponseEntity.ok().build();
    }
}
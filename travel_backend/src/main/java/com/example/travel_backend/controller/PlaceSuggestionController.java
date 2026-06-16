package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.PlaceSuggestionRequestDto;
import com.example.travel_backend.dto.response.PlaceSuggestionResponseDto;
import com.example.travel_backend.service.PlaceSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/place-suggestions")
public class PlaceSuggestionController {

    @Autowired
    private PlaceSuggestionService placeSuggestionService;

    @PostMapping
    public ResponseEntity<PlaceSuggestionResponseDto> createSuggestion(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PlaceSuggestionRequestDto request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        PlaceSuggestionResponseDto created = placeSuggestionService.createSuggestion(userId, request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<PlaceSuggestionResponseDto>> getMySuggestions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Page<PlaceSuggestionResponseDto> mySuggestions = placeSuggestionService.getMySuggestions(userId, page, size);
        return ResponseEntity.ok(mySuggestions);
    }
}

package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.PlaceSuggestionRequestDto;
import com.example.travel_backend.dto.response.PlaceSuggestionResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PlaceSuggestionService {
    PlaceSuggestionResponseDto createSuggestion(UUID userId, PlaceSuggestionRequestDto request);
    Page<PlaceSuggestionResponseDto> getMySuggestions(UUID userId, int page, int size);
}

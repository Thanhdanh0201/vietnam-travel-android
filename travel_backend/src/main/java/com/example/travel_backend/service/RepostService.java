package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.RepostRequestDto;
import java.util.UUID;

public interface RepostService {
    void createRepost(UUID userId, RepostRequestDto request);
    void deleteRepost(UUID userId, UUID postId);
}
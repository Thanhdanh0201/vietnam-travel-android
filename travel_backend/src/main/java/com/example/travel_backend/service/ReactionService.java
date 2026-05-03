package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.ReactionRequestDto;
import java.util.UUID;

public interface ReactionService {
    void likePost(UUID userId, ReactionRequestDto request);
    void unlikePost(UUID userId, UUID postId);
}
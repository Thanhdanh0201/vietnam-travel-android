package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.CommentReactionRequestDto;
import com.example.travel_backend.dto.request.CommentRequestDto;

import java.util.UUID;

public interface CommentService {
    void createComment(UUID userId, CommentRequestDto request);
    void likeComment(UUID userId, CommentReactionRequestDto request);
    void unlikeComment(UUID userId, UUID commentId);
}
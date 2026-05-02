package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ReactionRequestDto {
    private UUID postId;
    private String reactionType;
}
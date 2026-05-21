package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CommentReactionRequestDto {
    private UUID commentId;
    private String reactionType;
}
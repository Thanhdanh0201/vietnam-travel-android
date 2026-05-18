package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CommentRequestDto {
    private UUID postId;
    private UUID parentCommentId; // Null nếu là Top-level comment
    private String content;
}
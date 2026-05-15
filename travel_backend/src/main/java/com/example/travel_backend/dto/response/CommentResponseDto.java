package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CommentResponseDto {
    private UUID id;
    private UUID postId;
    private UUID parentCommentId;
    private String content;
    private OffsetDateTime createdAt;

    // Thông tin author (Tương đương view comments_with_author)
    private UUID authorId;
    private String authorName;
    private String authorAvatarUrl;

    // Thống kê (Dựa vào trigger dưới DB)
    private Integer replyCount;
    private Integer likeCount;
}
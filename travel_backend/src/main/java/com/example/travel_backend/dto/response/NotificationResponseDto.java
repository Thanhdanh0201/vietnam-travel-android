package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class NotificationResponseDto {
    private UUID id;
    private String type;
    private String previewText;
    private String reactionType;
    private Boolean isRead;
    private OffsetDateTime createdAt;

    private UUID postId;
    private UUID commentId;
    private UUID achievementId;

    private UUID itineraryId;
    private String itineraryTitle;
    private UUID placeSuggestionId;

    // Thông tin Actor (người gây ra thông báo)
    private UUID actorId;
    private String actorName;
    private String actorUsername;
    private String actorAvatarUrl;
    private String groupKey;
}
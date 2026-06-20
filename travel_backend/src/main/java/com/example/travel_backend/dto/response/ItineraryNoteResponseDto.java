package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ItineraryNoteResponseDto {
    private UUID id;
    private UUID itineraryId;
    private UUID itineraryItemId;  // null = ghi chú chung
    private UUID userId;
    private String userName;
    private String userAvatar;
    private String content;
    private String imageUrl;
    private OffsetDateTime createdAt;
}

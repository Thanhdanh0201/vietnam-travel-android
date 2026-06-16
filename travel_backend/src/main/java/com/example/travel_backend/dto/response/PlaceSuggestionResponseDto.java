package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PlaceSuggestionResponseDto {
    private UUID id;
    private String name;
    private String provinceName;
    private UUID provinceId;
    private Double lat;
    private Double lng;
    private String type;
    private String description;
    private String imageUrl;
    private String status;
    private String adminNote;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime createdAt;

    // Người đề xuất
    private UUID userId;
    private String userName;
    private String userAvatar;
}

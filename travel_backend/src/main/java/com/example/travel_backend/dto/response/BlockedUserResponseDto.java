package com.example.travel_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockedUserResponseDto {
    private UUID userId;
    private String name;
    private String avatarUrl;
    private OffsetDateTime blockedAt;
}
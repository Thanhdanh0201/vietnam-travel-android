package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class FollowingResponseDto {
    private UUID followingId;
    private OffsetDateTime createdAt;
    private UserCompactDto following;
}
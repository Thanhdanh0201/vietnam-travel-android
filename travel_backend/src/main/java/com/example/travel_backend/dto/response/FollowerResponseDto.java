package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class FollowerResponseDto {
    private UUID followerId;
    private OffsetDateTime createdAt;
    private UserCompactDto follower;
}

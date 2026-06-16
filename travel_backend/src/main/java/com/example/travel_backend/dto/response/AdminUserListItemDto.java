package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AdminUserListItemDto {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private Integer postCount;
    private Integer followerCount;
    private Boolean isVerified;
    private Boolean isBanned;
    private String bannedReason;
    private OffsetDateTime bannedAt;
    private String role;
    private OffsetDateTime createdAt;
}

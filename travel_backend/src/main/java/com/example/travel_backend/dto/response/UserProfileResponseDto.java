package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class UserProfileResponseDto {
    private UUID id;
    private String name;
    private String avatarUrl;
    private String coverUrl;
    private String bio;
    private String username;
    private String websiteUrl;
    private String explorerLevel;
    private Integer totalProvinces;
    private Integer totalPlacesVisited;
    private Integer followerCount;
    private Integer followingCount;
    private Integer postCount;
    private Boolean isVerified;
    private Boolean isPrivate;
    private Boolean isBanned;
    private String role;
}

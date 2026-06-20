package com.example.travel_backend.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class UserInviteSearchDto {
    private UUID id;
    private String name;
    private String username;
    private String avatarUrl;
    private Boolean isVerified;
}

package com.example.travel_backend.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequestDto {
    private String name;
    private String bio;
    private String avatarUrl;
    private String coverUrl;
    private Boolean isPrivate;
}
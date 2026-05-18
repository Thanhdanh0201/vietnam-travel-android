package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class UserCompactDto {
    private UUID id;
    private String name;
    private String avatarUrl;
    private String explorerLevel;
    private Boolean isVerified;
}

package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class PostMediaDto {
    private UUID id;
    private String mediaUrl;
    private String mediaType;
    private String thumbnailUrl;
    private Integer orderIndex;
}

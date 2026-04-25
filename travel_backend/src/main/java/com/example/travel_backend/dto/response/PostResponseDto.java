package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PostResponseDto {
    private UUID id;
    private String content;
    private String postType;
    private String visibility;
    private Integer reactionCount;
    private Integer commentCount;
    private Integer repostCount;
    private Boolean isEdited;
    private Boolean isPinned;
    private OffsetDateTime createdAt;

    private UserCompactDto author;
    private List<PostMediaDto> media;
}

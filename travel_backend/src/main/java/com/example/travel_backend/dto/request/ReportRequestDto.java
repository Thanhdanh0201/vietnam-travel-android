package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class ReportRequestDto {
    private String reason;
    private String description;
    private UUID reportedPostId;
    private UUID reportedCommentId;
    private UUID reportedUserId;
}
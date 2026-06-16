package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AdminReportResponseDto {
    private UUID id;
    private String reason;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime reviewedAt;

    // Reporter info
    private UUID reporterId;
    private String reporterName;
    private String reporterAvatar;

    // Reported content (one of the three will be non-null)
    private UUID reportedPostId;
    private String reportedPostContent;
    private String reportedPostAuthorName;

    private UUID reportedCommentId;
    private String reportedCommentContent;
    private String reportedCommentAuthorName;

    private UUID reportedUserId;
    private String reportedUserName;
    private String reportedUserAvatar;
}

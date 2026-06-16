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
    private UUID reportedPostAuthorId;
    private String reportedPostAuthorName;
    private String reportedPostAuthorAvatar;

    private UUID reportedCommentId;
    private UUID reportedCommentPostId;
    private String reportedCommentContent;
    private UUID reportedCommentAuthorId;
    private String reportedCommentAuthorName;
    private String reportedCommentAuthorAvatar;

    private UUID reportedUserId;
    private String reportedUserName;
    private String reportedUserAvatar;
}

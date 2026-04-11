package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class UserSettingResponseDto {
    private UUID id;
    private Boolean pushReactions;
    private Boolean pushComments;
    private Boolean pushFollows;
    private Boolean pushReposts;
    private Boolean pushMentions;
    private Boolean pushAchievements;
    private String language;
    private String theme;
}
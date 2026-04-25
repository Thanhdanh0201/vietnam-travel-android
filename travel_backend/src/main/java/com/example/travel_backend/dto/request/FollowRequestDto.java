package com.example.travel_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.UUID;

@Data
public class FollowRequestDto {
    @JsonProperty("follower_id")
    private UUID followerId;

    @JsonProperty("following_id")
    private UUID followingId;
}

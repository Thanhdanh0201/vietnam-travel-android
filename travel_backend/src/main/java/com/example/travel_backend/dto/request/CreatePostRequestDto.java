package com.example.travel_backend.dto.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreatePostRequestDto {
    private String content;
    private String postType; // text, review, itinerary_share...
    private String visibility; // public, private, friends
    private UUID placeId;
    private UUID itineraryId;
    private List<MediaRequestDto> media;

    @Data
    public static class MediaRequestDto {
        private String mediaUrl;
        private String mediaType;
        private String thumbnailUrl;
        private Integer orderIndex;
    }
}
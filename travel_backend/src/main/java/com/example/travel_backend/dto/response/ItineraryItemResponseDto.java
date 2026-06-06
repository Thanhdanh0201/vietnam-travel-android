package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class ItineraryItemResponseDto {
    private UUID id;
    private UUID placeId;
    private String placeName;
    private String imageUrl;
    private String tag;
    private String location;
    private LocalTime scheduledTime;
    private String day;
    private String note;
    private Integer orderIndex;
}

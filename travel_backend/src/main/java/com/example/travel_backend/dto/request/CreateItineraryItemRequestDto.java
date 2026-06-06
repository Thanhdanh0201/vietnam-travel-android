package com.example.travel_backend.dto.request;

import lombok.Data;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateItineraryItemRequestDto {
    private UUID placeId;
    private LocalTime scheduledTime;
    private String day;
    private String note;
    private Integer orderIndex;
}

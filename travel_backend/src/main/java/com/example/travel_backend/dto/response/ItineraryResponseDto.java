package com.example.travel_backend.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ItineraryResponseDto {
    private UUID id;
    private String title;
    private String location;
    private String description;
    private String coverUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer shareCount;
    private Integer itemCount;
    private OffsetDateTime createdAt;
    private String status;
    /** Role của user hiện tại đang query: "OWNER", "EDIT", "VIEW" */
    private String myRole;
    /** ID của chủ sở hữu itinerary */
    private UUID ownerId;
}


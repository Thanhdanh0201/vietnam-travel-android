package com.example.travel_backend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateItineraryRequestDto {
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private String coverUrl;
    private Boolean isPublic;
}

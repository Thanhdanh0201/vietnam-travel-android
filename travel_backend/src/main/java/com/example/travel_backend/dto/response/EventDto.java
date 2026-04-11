package com.example.travel_backend.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class EventDto {
    private UUID id;
    private String name;
    private String start_date;
    private String end_date;
}
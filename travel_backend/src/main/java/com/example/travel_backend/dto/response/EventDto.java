package com.example.travel_backend.dto.response;

import lombok.Data;

import java.util.UUID;

/**
 * Flat JSON for mobile clients (snake_case fields match existing API style).
 */
@Data
public class EventDto {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private String start_date;
    private String end_date;
    private String province_name;
    private String place_name;
}
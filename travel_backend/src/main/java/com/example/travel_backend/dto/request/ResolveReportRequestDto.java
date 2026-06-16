package com.example.travel_backend.dto.request;

import lombok.Data;

@Data
public class ResolveReportRequestDto {
    // "reviewed" | "resolved" | "dismissed"
    private String action;
    private String adminNote;
}

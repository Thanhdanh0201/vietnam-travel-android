package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.ReportRequestDto;

import java.util.UUID;

public interface ReportService {
    public void createReport(UUID reporterId, ReportRequestDto request);
}

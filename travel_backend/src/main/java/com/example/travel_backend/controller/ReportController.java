package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.ReportRequestDto;
import com.example.travel_backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> createReport(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReportRequestDto request) {
        UUID myId = UUID.fromString(jwt.getSubject());
        reportService.createReport(myId, request);
        return ResponseEntity.ok().build();
    }
}

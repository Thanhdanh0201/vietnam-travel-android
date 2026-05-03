package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.NotificationPatchDto;
import com.example.travel_backend.dto.response.NotificationResponseDto;
import com.example.travel_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "limit", defaultValue = "30") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(notificationService.getNotifications(myId, limit, offset));
    }

    @PatchMapping
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody NotificationPatchDto request) {

        if (Boolean.TRUE.equals(request.getIsRead())) {
            UUID myId = UUID.fromString(jwt.getSubject());
            notificationService.markAllAsRead(myId);
        }
        return ResponseEntity.ok().build();
    }
}

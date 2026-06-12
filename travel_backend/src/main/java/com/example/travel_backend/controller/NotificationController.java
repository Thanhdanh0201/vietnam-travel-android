package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.NotificationPatchDto;
import com.example.travel_backend.dto.response.NotificationResponseDto;
import com.example.travel_backend.dto.response.UnreadCountResponseDto;
import com.example.travel_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "category", required = false) String category) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(notificationService.getNotifications(myId, limit, offset, category));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponseDto> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        UUID myId = UUID.fromString(jwt.getSubject());
        UnreadCountResponseDto dto = new UnreadCountResponseDto();
        dto.setCount(notificationService.getUnreadCount(myId));
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> markSingleAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID notifId,
            @RequestBody NotificationPatchDto request) {

        if (Boolean.TRUE.equals(request.getIsRead())) {
            UUID myId = UUID.fromString(jwt.getSubject());
            notificationService.markAsRead(notifId, myId);
        }
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody NotificationPatchDto request) {

        if (Boolean.TRUE.equals(request.getIsRead())) {
            UUID myId = UUID.fromString(jwt.getSubject());
            notificationService.markAllAsRead(myId);
        }
        return ResponseEntity.ok().build();
    }
}

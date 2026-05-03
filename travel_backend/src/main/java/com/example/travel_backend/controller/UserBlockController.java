package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.BlockedUserResponseDto;
import com.example.travel_backend.service.UserBlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/blocks")
public class UserBlockController {

    @Autowired
    private UserBlockService userBlockService;

    // GET /api/blocks - Get list of users blocked by the current user
    @GetMapping
    public ResponseEntity<List<BlockedUserResponseDto>> getBlockedUsers(
            @AuthenticationPrincipal Jwt jwt) {

        UUID currentUserId = UUID.fromString(jwt.getSubject());
        List<BlockedUserResponseDto> blockedUsers = userBlockService.getBlockedUsers(currentUserId);

        return ResponseEntity.ok(blockedUsers);
    }

    // POST /api/blocks/{blockedId} - Block a user
    @PostMapping("/{blockedId}")
    public ResponseEntity<Void> blockUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID blockedId) {

        UUID blockerId = UUID.fromString(jwt.getSubject());
        userBlockService.blockUser(blockerId, blockedId);

        return ResponseEntity.ok().build();
    }

    // DELETE /api/blocks/{blockedId} - Unblock a user
    @DeleteMapping("/{blockedId}")
    public ResponseEntity<Void> unblockUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID blockedId) {

        UUID blockerId = UUID.fromString(jwt.getSubject());
        userBlockService.unblockUser(blockerId, blockedId);

        return ResponseEntity.ok().build();
    }
}
package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.UpdateProfileRequestDto;
import com.example.travel_backend.dto.response.UserProfileResponseDto;
import com.example.travel_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDto> getProfile(@PathVariable("id") UUID userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateProfileRequestDto request) {

        UUID userId = UUID.fromString(jwt.getSubject());
        try {
            return ResponseEntity.ok(userService.updateProfile(userId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
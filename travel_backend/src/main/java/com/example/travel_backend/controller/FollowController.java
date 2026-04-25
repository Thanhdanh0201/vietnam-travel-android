package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.FollowRequestDto;
import com.example.travel_backend.dto.response.FollowerResponseDto;
import com.example.travel_backend.dto.response.FollowingResponseDto;
import com.example.travel_backend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping
    public ResponseEntity<?> follow(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FollowRequestDto request) {

        UUID currentUserId = UUID.fromString(jwt.getSubject());

        // Bảo mật: Nếu ID gửi lên không phải là mình, từ chối luôn
        if (!currentUserId.equals(request.getFollowerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Security Violation: You cannot follow on behalf of another user.");
        }

        followService.followUser(request.getFollowerId(), request.getFollowingId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> unfollow(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("follower_id") UUID followerId,
            @RequestParam("following_id") UUID followingId) {

        UUID currentUserId = UUID.fromString(jwt.getSubject());

        if (!currentUserId.equals(followerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        followService.unfollowUser(followerId, followingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFollow(
            @RequestParam("follower_id") UUID followerId,
            @RequestParam("following_id") UUID followingId) {

        return ResponseEntity.ok(followService.checkIsFollowing(followerId, followingId));
    }

    @GetMapping("/followers")
    public ResponseEntity<List<FollowerResponseDto>> getFollowers(
            @RequestParam("following_id") UUID followingId) {
        return ResponseEntity.ok(followService.getFollowers(followingId));
    }

    @GetMapping("/following")
    public ResponseEntity<List<FollowingResponseDto>> getFollowing(
            @RequestParam("follower_id") UUID followerId) {
        return ResponseEntity.ok(followService.getFollowing(followerId));
    }
}
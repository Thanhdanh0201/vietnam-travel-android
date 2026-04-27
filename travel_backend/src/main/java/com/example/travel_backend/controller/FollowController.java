package com.example.travel_backend.controller;

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
            @PathVariable UUID followingId){

        UUID currentUserId = UUID.fromString(jwt.getSubject());

        followService.followUser(currentUserId, followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> unfollow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID followingId) {

        UUID currentUserId = UUID.fromString(jwt.getSubject());

        followService.unfollowUser(currentUserId, followingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> checkFollow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("userId") UUID targetUserId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(followService.checkIsFollowing(myId, targetUserId));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowerResponseDto>> getFollowers(
            @PathVariable("userId") UUID userId) {

        return ResponseEntity.ok(followService.getFollowers(userId));
    }


    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowingResponseDto>> getFollowing(
            @PathVariable("userId") UUID userId) {

        return ResponseEntity.ok(followService.getFollowing(userId));
    }
}
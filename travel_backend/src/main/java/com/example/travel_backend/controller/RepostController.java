package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.RepostRequestDto;
import com.example.travel_backend.service.RepostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reposts")
public class RepostController {

    @Autowired
    private RepostService repostService;

    // POST /api/reposts (Dung chung cho ca Repost thuong va Quote)
    @PostMapping
    public ResponseEntity<Void> createRepost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RepostRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        repostService.createRepost(myId, request);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/reposts?post_id=...
    @DeleteMapping
    public ResponseEntity<Void> deleteRepost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("post_id") UUID postId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        repostService.deleteRepost(myId, postId);
        return ResponseEntity.ok().build();
    }
}
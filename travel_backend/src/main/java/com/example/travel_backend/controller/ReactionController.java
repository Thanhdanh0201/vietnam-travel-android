package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.ReactionRequestDto;
import com.example.travel_backend.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/post_reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping
    public ResponseEntity<Void> likePost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ReactionRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        reactionService.likePost(myId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlikePost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("post_id") UUID postId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        reactionService.unlikePost(myId, postId);
        return ResponseEntity.ok().build();
    }
}
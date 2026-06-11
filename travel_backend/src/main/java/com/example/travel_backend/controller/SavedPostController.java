package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.PostResponseDto;
import com.example.travel_backend.repository.SavedPostRepository;
import com.example.travel_backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/saved_posts")
public class SavedPostController {

    @Autowired
    private PostService postService;

    @Autowired
    private SavedPostRepository savedPostRepository;

    @PostMapping
    public ResponseEntity<Void> savePost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("post_id") UUID postId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        postService.savePost(myId, postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unsavePost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("post_id") UUID postId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        postService.unsavePost(myId, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getSavedPosts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        UUID myId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(postService.getSavedPosts(myId, limit, offset));
    }

    @GetMapping("/check-saved")
    public ResponseEntity<List<UUID>> checkSavedPosts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("post_ids") List<UUID> postIds) {

        UUID myId = UUID.fromString(jwt.getSubject());
        List<UUID> savedPostIds = savedPostRepository.findSavedPostIdsByUser(myId, postIds);
        return ResponseEntity.ok(savedPostIds);
    }
}

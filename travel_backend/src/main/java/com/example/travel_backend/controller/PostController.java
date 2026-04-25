package com.example.travel_backend.controller;

import com.example.travel_backend.dto.response.PostResponseDto;
import com.example.travel_backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/user")
    public ResponseEntity<List<PostResponseDto>> getUserPosts(
            @RequestParam("user_id") UUID userId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        List<PostResponseDto> posts = postService.getUserPosts(userId, limit, offset);
        return ResponseEntity.ok(posts);
    }
}
package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.CreatePostRequestDto;
import com.example.travel_backend.dto.response.PostResponseDto;
import com.example.travel_backend.repository.PostReactionRepository;
import com.example.travel_backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private PostReactionRepository postReactionRepository;

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPostById(
            @PathVariable("postId") UUID postId) {
        System.out.println("API Call: Get Post By Id: " + postId);
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/public")
    public ResponseEntity<List<PostResponseDto>> getPublicFeed(
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        System.out.println("API Call: Get Public Feed");
        return ResponseEntity.ok(postService.getPublicFeed(limit, offset));
    }

    // 4.1 Lay Feed Following (Chi hien thi bai cua nhung nguoi minh dang theo doi)
    @GetMapping("/following")
    public ResponseEntity<List<PostResponseDto>> getFollowingFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        UUID myId = UUID.fromString(jwt.getSubject());
        System.out.println("API Call: Get Following Feed for user " + myId);
        return ResponseEntity.ok(postService.getFollowingFeed(myId, limit, offset));
    }

    // Lay toan bo bai viet cua MOT user cu the (Dung khi vao trang ca nhan cua ai do)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponseDto>> getUserPosts(
            @PathVariable("userId") UUID userId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        System.out.println("API Call: Get User Posts for user " + userId);
        return ResponseEntity.ok(postService.getUserPosts(userId, limit, offset));
    }

    // 4.2 Dang bai viet moi (Kem theo danh sach media, itinerary, place...)
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreatePostRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        System.out.println("API Call: Create Post by user " + myId);
        PostResponseDto newPost = postService.createPost(myId, request);
        return ResponseEntity.ok(newPost);
    }

    @GetMapping("/reactions/check-likes")
    public ResponseEntity<List<UUID>> checkLikedPosts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("post_ids") List<UUID> postIds) {

        UUID myId = UUID.fromString(jwt.getSubject());
        // Giả sử ông gọi thẳng repository ở đây cho nhanh, hoặc bọc qua PostService
        List<UUID> likedPostIds = postReactionRepository.findLikedPostIdsByUser(myId, postIds);

        return ResponseEntity.ok(likedPostIds);
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("id") UUID postId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        postService.deletePost(myId, postId);
        return ResponseEntity.ok().build();
    }
}
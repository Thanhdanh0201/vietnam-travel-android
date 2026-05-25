package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.CreatePostRequestDto;
import com.example.travel_backend.dto.request.ReactionRequestDto;
import com.example.travel_backend.dto.response.PostResponseDto;
import java.util.List;
import java.util.UUID;

public interface PostService {
    List<PostResponseDto> getUserPosts(UUID userId, int limit, int offset);
    public List<PostResponseDto> getPublicFeed(int limit, int offset);
    public List<PostResponseDto> getFollowingFeed(UUID currentUserId, int limit, int offset);
    public PostResponseDto createPost(UUID userId, CreatePostRequestDto request);
    PostResponseDto getPostById(UUID postId);
    void deletePost(UUID userId, UUID postId);

}

package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.PostResponseDto;
import java.util.List;
import java.util.UUID;

public interface PostService {
    List<PostResponseDto> getUserPosts(UUID userId, int limit, int offset);
}

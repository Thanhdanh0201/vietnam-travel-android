package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.FollowerResponseDto;
import com.example.travel_backend.dto.response.FollowingResponseDto;

import java.util.List;
import java.util.UUID;

public interface FollowService {
    void followUser(UUID followerId, UUID followingId);
    void unfollowUser(UUID followerId, UUID followingId);
    boolean checkIsFollowing(UUID followerId, UUID followingId);
    List<FollowerResponseDto> getFollowers(UUID followingId);
    List<FollowingResponseDto> getFollowing(UUID followerId);
}
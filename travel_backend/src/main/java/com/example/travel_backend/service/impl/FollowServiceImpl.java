package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.FollowerResponseDto;
import com.example.travel_backend.dto.response.FollowingResponseDto;
import com.example.travel_backend.dto.response.UserCompactDto;
import com.example.travel_backend.entity.Follow;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.FollowRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.FollowService;
import com.example.travel_backend.service.NotificationTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationTriggerService notificationTriggerService;

    @Override
    @Transactional
    public void followUser(UUID followerId, UUID followingId) {
        // Chặn lỗi null ngay từ đầu
        if (followerId == null || followingId == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Follower ID and Following ID must not be null");
        }

        if (followerId.equals(followingId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "You cannot follow yourself.");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            System.out.println("User already followed this target, skipping to avoid duplicate...");
            return;
        }

        Follow follow = new Follow();
        // Dùng getReferenceById để không phải SELECT User lên, chỉ cần lấy ID làm khóa ngoại
        follow.setFollower(userRepository.getReferenceById(followerId));
        follow.setFollowing(userRepository.getReferenceById(followingId));
        follow.setCreatedAt(OffsetDateTime.now());

        followRepository.save(follow);
        notificationTriggerService.notifyFollow(followerId, followingId);
    }

    @Override
    @Transactional
    public void unfollowUser(UUID followerId, UUID followingId) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public boolean checkIsFollowing(UUID followerId, UUID followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public List<FollowerResponseDto> getFollowers(UUID followingId) {
        List<Follow> follows = followRepository.findByFollowingIdOrderByCreatedAtDesc(followingId);
        return follows.stream().map(follow -> {
            FollowerResponseDto dto = new FollowerResponseDto();
            dto.setFollowerId(follow.getFollower().getId());
            dto.setCreatedAt(follow.getCreatedAt());
            dto.setFollower(mapToUserCompact(follow.getFollower()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FollowingResponseDto> getFollowing(UUID followerId) {
        List<Follow> follows = followRepository.findByFollowerIdOrderByCreatedAtDesc(followerId);
        return follows.stream().map(follow -> {
            FollowingResponseDto dto = new FollowingResponseDto();
            dto.setFollowingId(follow.getFollowing().getId());
            dto.setCreatedAt(follow.getCreatedAt());
            dto.setFollowing(mapToUserCompact(follow.getFollowing()));
            return dto;
        }).collect(Collectors.toList());
    }

    private UserCompactDto mapToUserCompact(User user) {
        UserCompactDto dto = new UserCompactDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername() != null && !user.getUsername().isBlank()
                ? "@" + user.getUsername()
                : null);
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setExplorerLevel(user.getExplorerLevel());
        dto.setIsVerified(user.getIsVerified());
        return dto;
    }
}
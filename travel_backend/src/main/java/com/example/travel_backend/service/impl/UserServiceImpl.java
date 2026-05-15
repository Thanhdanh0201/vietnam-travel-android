package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.UpdateProfileRequestDto;
import com.example.travel_backend.dto.response.UserProfileResponseDto;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserProfileResponseDto getUserProfile(UUID userId) {
        System.out.println("Fetching profile for user ID: " + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDto(user);
    }

    @Override
    public UserProfileResponseDto updateProfile(UUID userId, UpdateProfileRequestDto request) {
        System.out.println("Updating profile for user ID: " + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getCoverUrl() != null) user.setCoverUrl(request.getCoverUrl());
        if (request.getIsPrivate() != null) user.setIsPrivate(request.getIsPrivate());

        User updatedUser = userRepository.save(user);
        System.out.println("Profile updated successfully");
        return convertToDto(updatedUser);
    }

    private UserProfileResponseDto convertToDto(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCoverUrl(user.getCoverUrl());
        dto.setBio(user.getBio());
        dto.setExplorerLevel(user.getExplorerLevel());
        dto.setTotalProvinces(user.getTotalProvinces());
        dto.setFollowerCount(user.getFollowerCount());
        dto.setFollowingCount(user.getFollowingCount());
        dto.setPostCount(user.getPostCount());
        dto.setIsVerified(user.getIsVerified());
        dto.setIsPrivate(user.getIsPrivate());
        return dto;
    }
}

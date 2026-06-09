package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.UpdateProfileRequestDto;
import com.example.travel_backend.dto.response.UserProfileResponseDto;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9._]{3,30}$");

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserProfileResponseDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }

    @Override
    public UserProfileResponseDto updateProfile(UUID userId, UpdateProfileRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getCoverUrl() != null) user.setCoverUrl(request.getCoverUrl());
        if (request.getIsPrivate() != null) user.setIsPrivate(request.getIsPrivate());

        if (request.getUsername() != null) {
            String normalized = normalizeUsername(request.getUsername());
            validateUsername(normalized);
            userRepository.findByUsernameIgnoreCase(normalized).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new IllegalArgumentException("Username đã được sử dụng");
                }
            });
            user.setUsername(normalized);
        }

        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    private UserProfileResponseDto convertToDto(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCoverUrl(user.getCoverUrl());
        dto.setBio(user.getBio());
        dto.setUsername(formatUsernameForDisplay(user));
        dto.setWebsiteUrl(null);
        dto.setExplorerLevel(user.getExplorerLevel());
        dto.setTotalProvinces(user.getTotalProvinces());
        dto.setTotalPlacesVisited(user.getTotalPlacesVisited());
        dto.setFollowerCount(user.getFollowerCount());
        dto.setFollowingCount(user.getFollowingCount());
        dto.setPostCount(user.getPostCount());
        dto.setIsVerified(user.getIsVerified());
        dto.setIsPrivate(user.getIsPrivate());
        return dto;
    }

    private String formatUsernameForDisplay(User user) {
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return "@" + user.getUsername();
        }
        return deriveUsername(user);
    }

    private String normalizeUsername(String raw) {
        if (raw == null) return null;
        String value = raw.trim().toLowerCase();
        if (value.startsWith("@")) {
            value = value.substring(1);
        }
        return value;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username chỉ gồm chữ thường, số, dấu chấm hoặc gạch dưới (3-30 ký tự)"
            );
        }
    }

    private String deriveUsername(User user) {
        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            return "@" + email.substring(0, email.indexOf("@")).toLowerCase();
        }
        String name = user.getName();
        if (name != null && !name.isBlank()) {
            return "@" + name.toLowerCase().replaceAll("\\s+", ".");
        }
        return "@user";
    }
}

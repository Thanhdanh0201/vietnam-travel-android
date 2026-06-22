package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.UpdateProfileRequestDto;
import com.example.travel_backend.dto.response.UserInviteSearchDto;
import com.example.travel_backend.dto.response.UserProfileResponseDto;
import java.util.List;
import java.util.UUID;

public interface UserService {
    UserProfileResponseDto getUserProfile(UUID userId, UUID viewerId);
    UserProfileResponseDto updateProfile(UUID userId, UpdateProfileRequestDto request);
    List<UserInviteSearchDto> searchForInvite(UUID currentUserId, String keyword, int limit);
}

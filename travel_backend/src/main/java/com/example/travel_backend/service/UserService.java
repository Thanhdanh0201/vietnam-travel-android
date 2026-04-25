package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.UpdateProfileRequestDto;
import com.example.travel_backend.dto.response.UserProfileResponseDto;
import java.util.UUID;

public interface UserService {
    UserProfileResponseDto getUserProfile(UUID userId);
    UserProfileResponseDto updateProfile(UUID userId, UpdateProfileRequestDto request);
}

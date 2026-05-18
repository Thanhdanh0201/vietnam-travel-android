package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.BlockedUserResponseDto;
import java.util.List;
import java.util.UUID;

public interface UserBlockService {
    void blockUser(UUID blockerId, UUID blockedId);
    void unblockUser(UUID blockerId, UUID blockedId);
    boolean checkIsBlocked(UUID blockerId, UUID blockedId);
    List<BlockedUserResponseDto> getBlockedUsers(UUID blockerId);
}
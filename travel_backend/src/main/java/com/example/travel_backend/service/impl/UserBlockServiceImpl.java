package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.BlockedUserResponseDto;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.entity.UserBlock;
import com.example.travel_backend.repository.UserBlockRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.UserBlockService;
import com.example.travel_backend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserBlockServiceImpl implements UserBlockService {

    @Autowired
    private UserBlockRepository userBlockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowService followService;

    @Override
    @Transactional
    public void blockUser(UUID blockerId, UUID blockedId) {

        if (blockerId.equals(blockedId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot block yourself.");
        }

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already blocked.");
        }

        UserBlock userBlock = new UserBlock();
        userBlock.setBlocker(userRepository.getReferenceById(blockerId));
        userBlock.setBlocked(userRepository.getReferenceById(blockedId));
        userBlock.setCreatedAt(OffsetDateTime.now());

        userBlockRepository.save(userBlock);
        System.out.println("Block successful: " + blockerId + " blocked " + blockedId);

        try {
            followService.unfollowUser(blockerId, blockedId);
            followService.unfollowUser(blockedId, blockerId);
            System.out.println("Mutual follows removed successfully.");
        } catch (Exception e) {
            System.out.println("Error removing follow relationship: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void unblockUser(UUID blockerId, UUID blockedId) {

        if (!userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Block relationship does not exist.");
        }

        userBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
        System.out.println("Unblock successful: " + blockerId + " unblocked " + blockedId);
    }

    @Override
    public boolean checkIsBlocked(UUID blockerId, UUID blockedId) {
        return userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Override
    public List<BlockedUserResponseDto> getBlockedUsers(UUID blockerId) {
        List<UserBlock> blocks = userBlockRepository.findByBlockerId(blockerId);

        return blocks.stream().map(block -> {
            User blockedUser = block.getBlocked();
            return new BlockedUserResponseDto(
                    blockedUser.getId(),
                    blockedUser.getName(),
                    blockedUser.getAvatarUrl(),
                    block.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }
}
package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.NotificationResponseDto;
import com.example.travel_backend.entity.Notification;
import com.example.travel_backend.repository.NotificationRepository;
import com.example.travel_backend.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponseDto> getNotifications(UUID userId, int limit, int offset) {
        int page = offset / limit;
        Page<Notification> notifPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, limit));

        return notifPage.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        System.out.println("Marking all notifications as read for user: " + userId);
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationResponseDto mapToDto(Notification notif) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notif.getId());
        dto.setType(notif.getType());
        dto.setPreviewText(notif.getPreviewText());
        dto.setReactionType(notif.getReactionType());
        dto.setIsRead(notif.getIsRead());
        dto.setCreatedAt(notif.getCreatedAt());

        if (notif.getPost() != null) dto.setPostId(notif.getPost().getId());
        if (notif.getComment() != null) dto.setCommentId(notif.getComment().getId());
        dto.setAchievementId(notif.getAchievementId());

        dto.setActorId(notif.getActor().getId());
        // Bỏ comment 2 dòng dưới và dùng hàm lấy tên/avatar đúng chuẩn của Entity User của ông nhé:
        dto.setActorName(notif.getActor().getName());
        dto.setActorAvatarUrl(notif.getActor().getAvatarUrl());

        return dto;
    }
}

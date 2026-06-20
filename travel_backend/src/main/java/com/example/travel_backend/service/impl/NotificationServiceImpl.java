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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Map<String, List<String>> CATEGORY_TYPES = Map.of(
            "interaction", List.of("follow", "reaction", "comment", "comment_reaction", "repost", "mention"),
            "travel", List.of("itinerary_invite", "itinerary_updated"),
            "system", List.of("achievement", "place_suggestion_approved")
    );

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponseDto> getNotifications(UUID userId, int limit, int offset, String category) {
        int page = offset / Math.max(limit, 1);
        PageRequest pageable = PageRequest.of(page, limit);

        Page<Notification> notifPage;
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            List<String> types = CATEGORY_TYPES.get(category.toLowerCase());
            if (types == null) {
                notifPage = notificationRepository.findActiveByUserIdOrderByCreatedAtDesc(userId, pageable);
            } else {
                notifPage = notificationRepository.findActiveByUserIdAndTypeInOrderByCreatedAtDesc(userId, types, pageable);
            }
        } else {
            notifPage = notificationRepository.findActiveByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return notifPage.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countActiveUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notifId, UUID userId) {
        notificationRepository.markAsRead(notifId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /** Soft delete: sets is_deleted = true; row stays in DB (same pattern as posts/comments). */
    @Override
    @Transactional
    public void deleteNotification(UUID notifId, UUID userId) {
        notificationRepository.softDeleteByIdAndUserId(notifId, userId);
    }

    /** Soft delete batch: sets is_deleted = true for each id owned by user. */
    @Override
    @Transactional
    public void deleteNotifications(List<UUID> ids, UUID userId) {
        if (ids == null || ids.isEmpty()) return;
        notificationRepository.softDeleteByIdInAndUserId(ids, userId);
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

        if (notif.getItinerary() != null) {
            dto.setItineraryId(notif.getItinerary().getId());
            dto.setItineraryTitle(notif.getItinerary().getTitle());
        }
        dto.setPlaceSuggestionId(notif.getPlaceSuggestionId());

        dto.setActorId(notif.getActor().getId());
        dto.setActorName(notif.getActor().getName());
        dto.setActorUsername(notif.getActor().getUsername());
        dto.setActorAvatarUrl(notif.getActor().getAvatarUrl());

        dto.setGroupKey(notif.getGroupKey() != null ? notif.getGroupKey() : computeGroupKey(notif));
        return dto;
    }

    private String computeGroupKey(Notification notif) {
        if (notif.getPost() == null) return null;
        String type = notif.getType();
        UUID postId = notif.getPost().getId();
        if ("reaction".equals(type) || "comment".equals(type) || "comment_reaction".equals(type)
                || "repost".equals(type) || "mention".equals(type)) {
            return type + ":" + postId;
        }
        return null;
    }
}

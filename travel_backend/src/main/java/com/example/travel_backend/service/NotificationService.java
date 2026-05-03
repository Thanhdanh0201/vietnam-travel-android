package com.example.travel_backend.service;

import com.example.travel_backend.dto.response.NotificationResponseDto;
import com.example.travel_backend.entity.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface NotificationService {
    public List<NotificationResponseDto> getNotifications(UUID userId, int limit, int offset);
    public void markAllAsRead(UUID userId);
}

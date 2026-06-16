package com.example.travel_backend.service;

import com.example.travel_backend.dto.request.BanUserRequestDto;
import com.example.travel_backend.dto.request.RejectSuggestionRequestDto;
import com.example.travel_backend.dto.request.ResolveReportRequestDto;
import com.example.travel_backend.dto.response.AdminReportResponseDto;
import com.example.travel_backend.dto.response.AdminUserListItemDto;
import com.example.travel_backend.dto.response.PlaceSuggestionResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AdminService {
    // Reports
    Page<AdminReportResponseDto> getReports(String status, int page, int size);
    void resolveReport(UUID reportId, ResolveReportRequestDto request);
    void deleteReportedPost(UUID reportId);
    void deleteReportedComment(UUID reportId);

    // Users
    Page<AdminUserListItemDto> getUsers(String keyword, Boolean isBanned, int page, int size);
    AdminUserListItemDto getUserDetail(UUID userId);
    void banUser(UUID userId, BanUserRequestDto request);
    void unbanUser(UUID userId);

    // Place Suggestions
    Page<PlaceSuggestionResponseDto> getPlaceSuggestions(String status, int page, int size);
    void approveSuggestion(UUID suggestionId, UUID adminId);
    void rejectSuggestion(UUID suggestionId, UUID adminId, RejectSuggestionRequestDto request);
}

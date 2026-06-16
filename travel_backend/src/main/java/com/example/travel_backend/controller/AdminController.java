package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.BanUserRequestDto;
import com.example.travel_backend.dto.request.RejectSuggestionRequestDto;
import com.example.travel_backend.dto.request.ResolveReportRequestDto;
import com.example.travel_backend.dto.response.AdminReportResponseDto;
import com.example.travel_backend.dto.response.AdminUserListItemDto;
import com.example.travel_backend.dto.response.PlaceSuggestionResponseDto;
import com.example.travel_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ─── Reports ──────────────────────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<Page<AdminReportResponseDto>> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminReportResponseDto> reports = adminService.getReports(status, page, size);
        return ResponseEntity.ok(reports);
    }

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<Void> resolveReport(
            @PathVariable UUID reportId,
            @RequestBody ResolveReportRequestDto request) {
        adminService.resolveReport(reportId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reports/{reportId}/post")
    public ResponseEntity<Void> deleteReportedPost(@PathVariable UUID reportId) {
        adminService.deleteReportedPost(reportId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reports/{reportId}/comment")
    public ResponseEntity<Void> deleteReportedComment(@PathVariable UUID reportId) {
        adminService.deleteReportedComment(reportId);
        return ResponseEntity.ok().build();
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserListItemDto>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isBanned,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AdminUserListItemDto> users = adminService.getUsers(keyword, isBanned, page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserListItemDto> getUserDetail(@PathVariable UUID userId) {
        AdminUserListItemDto user = adminService.getUserDetail(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable UUID userId,
            @RequestBody BanUserRequestDto request) {
        adminService.banUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<Void> unbanUser(@PathVariable UUID userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }

    // ─── Place Suggestions ────────────────────────────────────────────────────

    @GetMapping("/place-suggestions")
    public ResponseEntity<Page<PlaceSuggestionResponseDto>> getPlaceSuggestions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PlaceSuggestionResponseDto> suggestions = adminService.getPlaceSuggestions(status, page, size);
        return ResponseEntity.ok(suggestions);
    }

    @PatchMapping("/place-suggestions/{id}/approve")
    public ResponseEntity<Void> approveSuggestion(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        adminService.approveSuggestion(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/place-suggestions/{id}/reject")
    public ResponseEntity<Void> rejectSuggestion(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RejectSuggestionRequestDto request) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        adminService.rejectSuggestion(id, adminId, request);
        return ResponseEntity.ok().build();
    }
}

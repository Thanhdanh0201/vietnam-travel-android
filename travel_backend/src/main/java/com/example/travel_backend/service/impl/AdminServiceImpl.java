package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.BanUserRequestDto;
import com.example.travel_backend.dto.request.RejectSuggestionRequestDto;
import com.example.travel_backend.dto.request.ResolveReportRequestDto;
import com.example.travel_backend.dto.response.AdminReportResponseDto;
import com.example.travel_backend.dto.response.AdminUserListItemDto;
import com.example.travel_backend.dto.response.PlaceSuggestionResponseDto;
import com.example.travel_backend.entity.Place;
import com.example.travel_backend.entity.PlaceSuggestion;
import com.example.travel_backend.entity.Post;
import com.example.travel_backend.entity.Comment;
import com.example.travel_backend.entity.Report;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.*;
import com.example.travel_backend.service.AdminService;
import com.example.travel_backend.service.NotificationTriggerService;
import com.example.travel_backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlaceSuggestionRepository placeSuggestionRepository;
    @Autowired private PlaceRepository placeRepository;
    @Autowired private NotificationTriggerService notificationTriggerService;
    @Autowired private PostService postService;

    // ─── Reports ──────────────────────────────────────────────────────────────

    @Override
    public Page<AdminReportResponseDto> getReports(String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reports = (status == null || status.isBlank())
                ? reportRepository.findAll(pageable)
                : reportRepository.findByStatus(status, pageable);
        return reports.map(this::toReportDto);
    }

    @Override
    @Transactional
    public void resolveReport(UUID reportId, ResolveReportRequestDto request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        String action = request.getAction();
        if (action == null || action.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action is required");
        }
        OffsetDateTime now = OffsetDateTime.now();
        List<Report> pending = findPendingReportsForSameTarget(report);
        if (pending.isEmpty() && "pending".equals(report.getStatus())) {
            pending = List.of(report);
        }
        for (Report r : pending) {
            r.setStatus(action);
            r.setReviewedAt(now);
        }
        reportRepository.saveAll(pending);
    }

    private List<Report> findPendingReportsForSameTarget(Report report) {
        if (report.getReportedPost() != null) {
            return reportRepository.findByReportedPost_IdAndStatus(
                    report.getReportedPost().getId(), "pending");
        }
        if (report.getReportedComment() != null) {
            return reportRepository.findByReportedComment_IdAndStatus(
                    report.getReportedComment().getId(), "pending");
        }
        if (report.getReportedUser() != null) {
            return reportRepository.findByReportedUser_IdAndStatus(
                    report.getReportedUser().getId(), "pending");
        }
        return List.of(report);
    }

    @Override
    @Transactional
    public void deleteReportedPost(UUID reportId) {
        Report report = reportRepository.findByIdWithReportedPost(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        if (report.getReportedPost() == null) {
            report.setStatus("resolved");
            report.setReviewedAt(OffsetDateTime.now());
            reportRepository.save(report);
            return;
        }
        Post reportedPost = report.getReportedPost();
        if (Boolean.TRUE.equals(reportedPost.getIsDeleted())) {
            report.setStatus("resolved");
            report.setReviewedAt(OffsetDateTime.now());
            reportRepository.save(report);
            return;
        }
        UUID postId = reportedPost.getId();
        System.out.println("Admin deleteReportedPost reportId=" + reportId + " postId=" + postId);
        postService.forceDeletePost(postId);
        OffsetDateTime now = OffsetDateTime.now();
        reportRepository.resolveAllForPost(postId, "resolved", now);
    }

    @Override
    @Transactional
    public void deleteReportedComment(UUID reportId) {
        Report report = reportRepository.findByIdWithReportedComment(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        if (report.getReportedComment() == null) {
            report.setStatus("resolved");
            report.setReviewedAt(OffsetDateTime.now());
            reportRepository.save(report);
            return;
        }
        Comment reportedComment = report.getReportedComment();
        if (Boolean.TRUE.equals(reportedComment.getIsDeleted())) {
            report.setStatus("resolved");
            report.setReviewedAt(OffsetDateTime.now());
            reportRepository.save(report);
            return;
        }
        UUID commentId = reportedComment.getId();
        System.out.println("Admin deleteReportedComment reportId=" + reportId + " commentId=" + commentId);
        postService.forceDeleteCommentTree(commentId);
        OffsetDateTime now = OffsetDateTime.now();
        reportRepository.resolveAllForComment(commentId, "resolved", now);
    }

    // ─── Users ────────────────────────────────────────────────────────────────

    @Override
    public Page<AdminUserListItemDto> getUsers(String keyword, Boolean isBanned, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return userRepository.searchUsers(kw, isBanned, pageable).map(this::toUserDto);
    }

    @Override
    public AdminUserListItemDto getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toUserDto(user);
    }

    @Override
    @Transactional
    public void banUser(UUID userId, BanUserRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if ("admin".equals(user.getRole()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot ban an admin");
        user.setIsBanned(true);
        user.setBannedReason(request.getReason());
        user.setBannedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unbanUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setIsBanned(false);
        user.setBannedReason(null);
        user.setBannedAt(null);
        userRepository.save(user);
    }

    // ─── Place Suggestions ────────────────────────────────────────────────────

    @Override
    public Page<PlaceSuggestionResponseDto> getPlaceSuggestions(String status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PlaceSuggestion> suggestions = (status == null || status.isBlank())
                ? placeSuggestionRepository.findAll(pageable)
                : placeSuggestionRepository.findByStatus(status, pageable);
        return suggestions.map(this::toSuggestionDto);
    }

    @Override
    @Transactional
    public void approveSuggestion(UUID suggestionId, UUID adminId) {
        PlaceSuggestion suggestion = placeSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggestion not found"));

        if (suggestion.getProvince() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suggestion has no province; cannot create place");
        if (suggestion.getLat() == null || suggestion.getLng() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suggestion has no coordinates; cannot create place");
        if (suggestion.getType() == null || suggestion.getType().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suggestion has no type; cannot create place");

        Place place = new Place();
        place.setProvince(suggestion.getProvince());
        place.setName(suggestion.getName());
        place.setLat(suggestion.getLat());
        place.setLng(suggestion.getLng());
        place.setType(suggestion.getType());
        place.setDescription(suggestion.getDescription());
        place.setImageUrl(suggestion.getImageUrl());
        placeRepository.save(place);

        suggestion.setStatus("approved");
        suggestion.setReviewedAt(OffsetDateTime.now());
        suggestion.setReviewedBy(userRepository.getReferenceById(adminId));
        placeSuggestionRepository.save(suggestion);

        if (suggestion.getUser() != null) {
            notificationTriggerService.notifyPlaceSuggestionApproved(
                    adminId, suggestion.getUser().getId(), suggestionId);
        }
    }

    @Override
    @Transactional
    public void rejectSuggestion(UUID suggestionId, UUID adminId, RejectSuggestionRequestDto request) {
        PlaceSuggestion suggestion = placeSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggestion not found"));
        suggestion.setStatus("rejected");
        suggestion.setAdminNote(request.getAdminNote());
        suggestion.setReviewedAt(OffsetDateTime.now());
        suggestion.setReviewedBy(userRepository.getReferenceById(adminId));
        placeSuggestionRepository.save(suggestion);
    }

    // ─── Converters ───────────────────────────────────────────────────────────

    private AdminReportResponseDto toReportDto(Report r) {
        AdminReportResponseDto dto = new AdminReportResponseDto();
        dto.setId(r.getId());
        dto.setReason(r.getReason());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setReviewedAt(r.getReviewedAt());

        if (r.getReporter() != null) {
            dto.setReporterId(r.getReporter().getId());
            dto.setReporterName(r.getReporter().getName());
            dto.setReporterAvatar(r.getReporter().getAvatarUrl());
        }
        if (r.getReportedPost() != null) {
            dto.setReportedPostId(r.getReportedPost().getId());
            if (Boolean.TRUE.equals(r.getReportedPost().getIsDeleted())) {
                dto.setReportedPostContent("[Bài viết đã bị xóa]");
            } else {
                dto.setReportedPostContent(r.getReportedPost().getContent());
            }
            if (r.getReportedPost().getUser() != null) {
                dto.setReportedPostAuthorId(r.getReportedPost().getUser().getId());
                dto.setReportedPostAuthorName(r.getReportedPost().getUser().getName());
                dto.setReportedPostAuthorAvatar(r.getReportedPost().getUser().getAvatarUrl());
            }
        }
        if (r.getReportedComment() != null) {
            dto.setReportedCommentId(r.getReportedComment().getId());
            if (Boolean.TRUE.equals(r.getReportedComment().getIsDeleted())) {
                dto.setReportedCommentContent("[Bình luận đã bị xóa]");
            } else {
                dto.setReportedCommentContent(r.getReportedComment().getContent());
            }
            if (r.getReportedComment().getPost() != null) {
                dto.setReportedCommentPostId(r.getReportedComment().getPost().getId());
            }
            if (r.getReportedComment().getUser() != null) {
                dto.setReportedCommentAuthorId(r.getReportedComment().getUser().getId());
                dto.setReportedCommentAuthorName(r.getReportedComment().getUser().getName());
                dto.setReportedCommentAuthorAvatar(r.getReportedComment().getUser().getAvatarUrl());
            }
        }
        if (r.getReportedUser() != null) {
            dto.setReportedUserId(r.getReportedUser().getId());
            dto.setReportedUserName(r.getReportedUser().getName());
            dto.setReportedUserAvatar(r.getReportedUser().getAvatarUrl());
        }
        return dto;
    }

    private AdminUserListItemDto toUserDto(User u) {
        AdminUserListItemDto dto = new AdminUserListItemDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setAvatarUrl(u.getAvatarUrl());
        dto.setPostCount(u.getPostCount());
        dto.setFollowerCount(u.getFollowerCount());
        dto.setIsVerified(u.getIsVerified());
        dto.setIsBanned(u.getIsBanned());
        dto.setBannedReason(u.getBannedReason());
        dto.setBannedAt(u.getBannedAt());
        dto.setRole(u.getRole());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }

    private PlaceSuggestionResponseDto toSuggestionDto(PlaceSuggestion s) {
        PlaceSuggestionResponseDto dto = new PlaceSuggestionResponseDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setLat(s.getLat());
        dto.setLng(s.getLng());
        dto.setType(s.getType());
        dto.setDescription(s.getDescription());
        dto.setImageUrl(s.getImageUrl());
        dto.setStatus(s.getStatus());
        dto.setAdminNote(s.getAdminNote());
        dto.setReviewedAt(s.getReviewedAt());
        dto.setCreatedAt(s.getCreatedAt());
        if (s.getProvince() != null) {
            dto.setProvinceId(s.getProvince().getId());
            dto.setProvinceName(s.getProvince().getName());
        }
        if (s.getUser() != null) {
            dto.setUserId(s.getUser().getId());
            dto.setUserName(s.getUser().getName());
            dto.setUserAvatar(s.getUser().getAvatarUrl());
        }
        return dto;
    }
}

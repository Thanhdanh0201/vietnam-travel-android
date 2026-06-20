package com.example.travel_backend.controller;

import com.example.travel_backend.dto.request.CommentReactionRequestDto;
import com.example.travel_backend.dto.request.CommentRequestDto;
import com.example.travel_backend.dto.response.CommentResponseDto;
import com.example.travel_backend.entity.Comment;
import com.example.travel_backend.repository.CommentReactionRepository;
import com.example.travel_backend.repository.CommentRepository;
import com.example.travel_backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentReactionRepository commentReactionRepository;

    // --- 1. LẤY DANH SÁCH BÌNH LUẬN ---
    @GetMapping("/comments_with_author")
    public ResponseEntity<?> getComments(
            @RequestParam(value = "post_id", required = false) UUID postId,
            @RequestParam(value = "parent_comment_id", required = false) UUID parentCommentId,
            @RequestParam(value = "limit", defaultValue = "50") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset) {

        System.out.println("Fetching comments...");

        // Chuyển đổi limit/offset sang Pageable
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);

        List<CommentResponseDto> responseList;

        if (postId != null) {
            // Lấy Top-level comments
            Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(postId, pageable);
            responseList = commentPage.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        } else if (parentCommentId != null) {
            // Lấy Replies (Thường replies không cần phân trang gắt gao như top-level)
            List<Comment> replies = commentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(parentCommentId);
            responseList = replies.stream().map(this::mapToDto).collect(Collectors.toList());
        } else {
            return ResponseEntity.badRequest().body("Must provide either post_id or parent_comment_id");
        }

        return ResponseEntity.ok(responseList);
    }

    // --- 2. KIỂM TRA ĐÃ LIKE COMMENT CHƯA ---
    @GetMapping("/comment_reactions/check-likes")
    public ResponseEntity<List<UUID>> checkLikedComments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("comment_ids") List<UUID> commentIds) {

        UUID myId = UUID.fromString(jwt.getSubject());
        System.out.println("Checking liked comments for user: " + myId);

        List<UUID> likedIds = commentReactionRepository.findLikedCommentIdsByUser(myId, commentIds);
        return ResponseEntity.ok(likedIds);
    }

    @Autowired
    private CommentService commentService;

    // --- 4.7 ĐĂNG BÌNH LUẬN ---
    @PostMapping("/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CommentRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        Comment newComment = commentService.createComment(myId, request);
        return ResponseEntity.ok(mapToDto(newComment));
    }

    // --- 4.8 LIKE BÌNH LUẬN ---
    @PostMapping("/comment_reactions")
    public ResponseEntity<Void> likeComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CommentReactionRequestDto request) {

        UUID myId = UUID.fromString(jwt.getSubject());
        commentService.likeComment(myId, request);
        return ResponseEntity.ok().build();
    }

    // --- 4.8 UNLIKE BÌNH LUẬN ---
    @DeleteMapping("/comment_reactions")
    public ResponseEntity<Void> unlikeComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("comment_id") UUID commentId) {

        UUID myId = UUID.fromString(jwt.getSubject());
        commentService.unlikeComment(myId, commentId);
        return ResponseEntity.ok().build();
    }

    // --- HÀM HỖ TRỢ MAP ENTITY SANG DTO ---
    private CommentResponseDto mapToDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPost().getId());
        dto.setContent(comment.getContent());
        dto.setImageUrl(comment.getImageUrl());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }

        // Map Author
        dto.setAuthorId(comment.getUser().getId());
        dto.setAuthorName(comment.getUser().getName());
        dto.setAuthorAvatarUrl(comment.getUser().getAvatarUrl());

        // Map Counts
        dto.setReplyCount(comment.getReplyCount() != null ? comment.getReplyCount() : 0);
        dto.setLikeCount(comment.getReactionCount() != null ? comment.getReactionCount() : 0);

        return dto;
    }
}
package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.CommentReactionRequestDto;
import com.example.travel_backend.dto.request.CommentRequestDto;
import com.example.travel_backend.entity.Comment;
import com.example.travel_backend.entity.CommentReaction;
import com.example.travel_backend.repository.CommentReactionRepository;
import com.example.travel_backend.repository.CommentRepository;
import com.example.travel_backend.repository.PostRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.entity.Post;
import com.example.travel_backend.service.CommentService;
import com.example.travel_backend.service.NotificationTriggerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentReactionRepository commentReactionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationTriggerService notificationTriggerService;

    // --- 4.7: ĐĂNG BÌNH LUẬN ---
    @Override
    @Transactional
    public Comment createComment(UUID userId, CommentRequestDto request) {
        System.out.println("Creating comment for post: " + request.getPostId());

        Comment comment = new Comment();
        comment.setUser(userRepository.getReferenceById(userId));
        comment.setPost(postRepository.getReferenceById(request.getPostId()));
        comment.setContent(request.getContent());
        comment.setImageUrl(request.getImageUrl());

        // Kiểm tra nếu là Reply (có parent_comment_id)
        if (request.getParentCommentId() != null) {
            comment.setParentComment(commentRepository.getReferenceById(request.getParentCommentId()));
        }

        // Set mặc định các chỉ số và thời gian để tránh null khi trả về DTO
        comment.setReactionCount(0);
        comment.setReplyCount(0);
        comment.setIsEdited(false);
        comment.setCreatedAt(java.time.OffsetDateTime.now());
        comment.setUpdatedAt(java.time.OffsetDateTime.now());

        Comment saved = commentRepository.save(comment);

        Post post = postRepository.findById(request.getPostId()).orElse(null);
        if (post != null && post.getUser() != null) {
            notificationTriggerService.notifyComment(
                    userId, post.getUser().getId(), request.getPostId(), saved.getId(), request.getContent());
        }

        if (request.getParentCommentId() != null) {
            commentRepository.findById(request.getParentCommentId()).ifPresent(parent -> {
                if (parent.getUser() != null) {
                    notificationTriggerService.notifyComment(
                            userId, parent.getUser().getId(), request.getPostId(), saved.getId(), request.getContent());
                }
            });
        }

        return saved;
    }

    // --- 4.8: LIKE BÌNH LUẬN ---
    @Override
    @Transactional
    public void likeComment(UUID userId, CommentReactionRequestDto request) {
        System.out.println("Liking comment: " + request.getCommentId());

        CommentReaction reaction = new CommentReaction();
        reaction.setUser(userRepository.getReferenceById(userId));
        reaction.setComment(commentRepository.getReferenceById(request.getCommentId()));
        reaction.setReactionType(request.getReactionType() != null ? request.getReactionType() : "like");

        commentReactionRepository.save(reaction);

        commentRepository.findById(request.getCommentId()).ifPresent(comment -> {
            UUID commentOwnerId = comment.getUser() != null ? comment.getUser().getId() : null;
            UUID postId = comment.getPost() != null ? comment.getPost().getId() : null;
            if (commentOwnerId != null) {
                notificationTriggerService.notifyCommentReaction(
                        userId, commentOwnerId, request.getCommentId(), postId, reaction.getReactionType());
            }
        });
    }

    // --- 4.8: BỎ LIKE BÌNH LUẬN ---
    @Override
    @Transactional
    public void unlikeComment(UUID userId, UUID commentId) {
        System.out.println("Unliking comment: " + commentId);
        commentReactionRepository.deleteByUserIdAndCommentId(userId, commentId);
    }
}
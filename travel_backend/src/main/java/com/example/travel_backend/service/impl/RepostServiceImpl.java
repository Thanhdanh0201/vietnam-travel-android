package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.RepostRequestDto;
import com.example.travel_backend.entity.Repost; // Import class Repost
import com.example.travel_backend.repository.PostRepository;
import com.example.travel_backend.repository.RepostRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.entity.Post;
import com.example.travel_backend.service.NotificationTriggerService;
import com.example.travel_backend.service.RepostService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RepostServiceImpl implements RepostService {

    @Autowired
    private RepostRepository repostRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationTriggerService notificationTriggerService;

    @Override
    @Transactional
    public void createRepost(UUID userId, RepostRequestDto request) {
        System.out.println("Creating repost for post: " + request.getPostId());

        Post originalPost = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Original post not found"));

        Repost repost = new Repost();
        repost.setUser(userRepository.getReferenceById(userId));
        repost.setPost(originalPost);
        repost.setQuoteText(request.getQuoteText());
        repostRepository.save(repost);

        // Update repost count on the original post
        originalPost.setRepostCount((originalPost.getRepostCount() != null ? originalPost.getRepostCount() : 0) + 1);
        postRepository.save(originalPost);

        // Create a new Post in posts table representing the repost/quote
        Post newPost = new Post();
        newPost.setUser(userRepository.getReferenceById(userId));
        newPost.setContent(request.getQuoteText());
        newPost.setPostType(request.getQuoteText() != null && !request.getQuoteText().trim().isEmpty() ? "quote" : "repost");
        newPost.setVisibility("public");
        newPost.setOriginalPost(originalPost);
        newPost.setReactionCount(0);
        newPost.setCommentCount(0);
        newPost.setRepostCount(0);
        newPost.setIsEdited(false);
        newPost.setIsPinned(false);
        newPost.setCreatedAt(java.time.OffsetDateTime.now());
        newPost.setUpdatedAt(java.time.OffsetDateTime.now());
        postRepository.save(newPost);

        if (originalPost.getUser() != null) {
            notificationTriggerService.notifyRepost(userId, originalPost.getUser().getId(), originalPost.getId());
        }
    }

    @Override
    @Transactional
    public void deleteRepost(UUID userId, UUID postId) {
        System.out.println("Deleting repost for post: " + postId);

        // 1. Decrement repost count on the original post
        Post originalPost = postRepository.findById(postId).orElse(null);
        if (originalPost != null) {
            originalPost.setRepostCount(Math.max(0, (originalPost.getRepostCount() != null ? originalPost.getRepostCount() : 0) - 1));
            postRepository.save(originalPost);
        }

        // 2. Delete the created Post from posts table
        postRepository.deleteRepostPost(userId, postId);

        // 3. Delete from reposts table
        repostRepository.deleteByUserIdAndPostId(userId, postId);
    }
}
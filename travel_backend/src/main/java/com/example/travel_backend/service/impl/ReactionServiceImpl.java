package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.ReactionRequestDto;
import com.example.travel_backend.entity.PostReaction;
import com.example.travel_backend.repository.PostReactionRepository;
import com.example.travel_backend.repository.PostRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.NotificationTriggerService;
import com.example.travel_backend.service.ReactionService;
import com.example.travel_backend.entity.Post;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ReactionServiceImpl implements ReactionService {

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationTriggerService notificationTriggerService;

    @Override
    @Transactional
    public void likePost(UUID userId, ReactionRequestDto request) {
        System.out.println("Liking post: " + request.getPostId());

        postRepository.findByIdAndIsDeletedFalse(request.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        PostReaction reaction = new PostReaction();
        reaction.setUser(userRepository.getReferenceById(userId));
        reaction.setPost(postRepository.getReferenceById(request.getPostId()));
        reaction.setReactionType(request.getReactionType() != null ? request.getReactionType() : "like");

        postReactionRepository.save(reaction);

        Post post = postRepository.findById(request.getPostId()).orElse(null);
        if (post != null && post.getUser() != null) {
            notificationTriggerService.notifyReaction(
                    userId, post.getUser().getId(), request.getPostId(), reaction.getReactionType());
        }
    }

    @Override
    @Transactional
    public void unlikePost(UUID userId, UUID postId) {
        System.out.println("Unliking post: " + postId);
        postReactionRepository.deleteByUserIdAndPostId(userId, postId);
    }
}
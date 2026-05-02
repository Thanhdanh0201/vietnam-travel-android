package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.ReactionRequestDto;
import com.example.travel_backend.entity.PostReaction;
import com.example.travel_backend.repository.PostReactionRepository;
import com.example.travel_backend.repository.PostRepository;
import com.example.travel_backend.repository.UserRepository;
import com.example.travel_backend.service.ReactionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReactionServiceImpl implements ReactionService {

    @Autowired
    private PostReactionRepository postReactionRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void likePost(UUID userId, ReactionRequestDto request) {
        System.out.println("Liking post: " + request.getPostId());

        PostReaction reaction = new PostReaction();
        reaction.setUser(userRepository.getReferenceById(userId));
        reaction.setPost(postRepository.getReferenceById(request.getPostId()));
        reaction.setReactionType(request.getReactionType() != null ? request.getReactionType() : "like");

        postReactionRepository.save(reaction);
    }

    @Override
    @Transactional
    public void unlikePost(UUID userId, UUID postId) {
        System.out.println("Unliking post: " + postId);
        postReactionRepository.deleteByUserIdAndPostId(userId, postId);
    }
}
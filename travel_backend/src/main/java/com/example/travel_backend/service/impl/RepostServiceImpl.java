package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.RepostRequestDto;
import com.example.travel_backend.entity.Repost; // Import class Repost
import com.example.travel_backend.repository.PostRepository;
import com.example.travel_backend.repository.RepostRepository;
import com.example.travel_backend.repository.UserRepository;
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

    @Override
    @Transactional
    public void createRepost(UUID userId, RepostRequestDto request) {
        System.out.println("Creating repost for post: " + request.getPostId());

        Repost repost = new Repost();
        repost.setUser(userRepository.getReferenceById(userId));
        repost.setPost(postRepository.getReferenceById(request.getPostId()));
        repost.setQuoteText(request.getQuoteText());

        repostRepository.save(repost);
    }

    @Override
    @Transactional
    public void deleteRepost(UUID userId, UUID postId) {
        System.out.println("Deleting repost for post: " + postId);

        repostRepository.deleteByUserIdAndPostId(userId, postId);
    }
}
package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.response.PostMediaDto;
import com.example.travel_backend.dto.response.PostResponseDto;
import com.example.travel_backend.dto.response.UserCompactDto;
import com.example.travel_backend.entity.Post;
import com.example.travel_backend.entity.PostMedia;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.PostMediaRepository;
import com.example.travel_backend.repository.PostRepository;
import com.example.travel_backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Override
    public List<PostResponseDto> getUserPosts(UUID userId, int limit, int offset) {
        System.out.println("Fetching posts for user: " + userId + " | limit: " + limit + ", offset: " + offset);

        // Convert offset to page number for Spring Data JPA
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);

        Page<Post> postPage = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Post> posts = postPage.getContent();

        if (posts.isEmpty()) {
            return new ArrayList<>();
        }

        // Tinh chinh Hieu Nang: Fetch tat ca media cua cac post cung 1 luc
        List<UUID> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<PostMedia> allMedia = postMediaRepository.findByPostIdInOrderByOrderIndexAsc(postIds);

        // Nhom media theo tung postId
        Map<UUID, List<PostMedia>> mediaByPostId = allMedia.stream()
                .collect(Collectors.groupingBy(media -> media.getPost().getId()));

        return posts.stream().map(post -> {
            PostResponseDto dto = mapToPostDto(post);

            List<PostMedia> postMediaList = mediaByPostId.getOrDefault(post.getId(), new ArrayList<>());
            List<PostMediaDto> mediaDtos = postMediaList.stream()
                    .map(this::mapToMediaDto)
                    .collect(Collectors.toList());

            dto.setMedia(mediaDtos);
            return dto;
        }).collect(Collectors.toList());
    }

    private PostResponseDto mapToPostDto(Post post) {
        PostResponseDto dto = new PostResponseDto();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setPostType(post.getPostType());
        dto.setVisibility(post.getVisibility());
        dto.setReactionCount(post.getReactionCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setRepostCount(post.getRepostCount());
        dto.setIsEdited(post.getIsEdited());
        dto.setIsPinned(post.getIsPinned());
        dto.setCreatedAt(post.getCreatedAt());

        dto.setAuthor(mapToUserCompact(post.getUser()));
        return dto;
    }

    private PostMediaDto mapToMediaDto(PostMedia media) {
        PostMediaDto dto = new PostMediaDto();
        dto.setId(media.getId());
        dto.setMediaUrl(media.getMediaUrl());
        dto.setMediaType(media.getMediaType());
        dto.setThumbnailUrl(media.getThumbnailUrl());
        dto.setOrderIndex(media.getOrderIndex());
        return dto;
    }

    private UserCompactDto mapToUserCompact(User user) {
        UserCompactDto dto = new UserCompactDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setExplorerLevel(user.getExplorerLevel());
        dto.setIsVerified(user.getIsVerified());
        return dto;
    }
}
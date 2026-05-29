package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.CreatePostRequestDto;
import com.example.travel_backend.dto.response.PostMediaDto;
import com.example.travel_backend.dto.response.PostResponseDto;
import com.example.travel_backend.dto.response.UserCompactDto;
import com.example.travel_backend.entity.Post;
import com.example.travel_backend.entity.PostMedia;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.repository.*;
import com.example.travel_backend.service.PostService;
import jakarta.transaction.Transactional;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;


    @Override
    public List<PostResponseDto> getUserPosts(UUID userId, int limit, int offset) {
        System.out.println("Fetching posts for user: " + userId + " | limit: " + limit + ", offset: " + offset);

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);

        Page<Post> postPage = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return mapPostPageToDtoList(postPage.getContent());
    }

    // --- 4.1 Lấy Community Feed ---
    @Override
    public List<PostResponseDto> getPublicFeed(int limit, int offset) {
        System.out.println("Fetching public feed | limit: " + limit + ", offset: " + offset);
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Post> postPage = postRepository.findByVisibilityOrderByCreatedAtDesc("public", pageable);
        return mapPostPageToDtoList(postPage.getContent());
    }

    @Override
    public List<PostResponseDto> getFollowingFeed(UUID currentUserId, int limit, int offset) {
        System.out.println("Fetching following feed for user: " + currentUserId);
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Post> postPage = postRepository.findFollowingPosts(currentUserId, pageable);
        return mapPostPageToDtoList(postPage.getContent());
    }

    // Tach logic map list de tai su dung cho ca 3 ham get Feed
    private List<PostResponseDto> mapPostPageToDtoList(List<Post> posts) {
        if (posts.isEmpty()) return new ArrayList<>();
        List<UUID> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<PostMedia> allMedia = postMediaRepository.findByPostIdInOrderByOrderIndexAsc(postIds);
        Map<UUID, List<PostMedia>> mediaByPostId = allMedia.stream()
                .collect(Collectors.groupingBy(media -> media.getPost().getId()));

        return posts.stream().map(post -> {
            PostResponseDto dto = mapToPostDto(post);
            List<PostMedia> postMediaList = mediaByPostId.getOrDefault(post.getId(), new ArrayList<>());
            dto.setMedia(postMediaList.stream().map(this::mapToMediaDto).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    // --- 4.2 Tạo bài đăng mới ---
    @Override
    @Transactional
    public PostResponseDto createPost(UUID userId, CreatePostRequestDto request) {
        System.out.println("Creating new post for user: " + userId);
        Post post = new Post();
        post.setUser(userRepository.getReferenceById(userId));
        post.setContent(request.getContent());
        post.setPostType(request.getPostType() != null ? request.getPostType() : "text");
        post.setVisibility(request.getVisibility() != null ? request.getVisibility() : "public");

        if (request.getItineraryId() != null) post.setItinerary(itineraryRepository.getReferenceById(request.getItineraryId()));
        if (request.getPlaceId() != null) post.setPlace(placeRepository.getReferenceById(request.getPlaceId()));

        // Set mặc định các chỉ số để tránh lỗi NullPointerException khi mapping
        post.setReactionCount(0);
        post.setCommentCount(0);
        post.setRepostCount(0);
        post.setIsEdited(false);
        post.setIsPinned(false);
        post.setCreatedAt(java.time.OffsetDateTime.now());
        post.setUpdatedAt(java.time.OffsetDateTime.now());

        Post savedPost = postRepository.save(post);
        List<PostMedia> savedMediaList = new ArrayList<>();

        // Luu Media
        if (request.getMedia() != null && !request.getMedia().isEmpty()) {
            for (var mediaReq : request.getMedia()) {
                PostMedia media = new PostMedia();
                media.setPost(savedPost);
                media.setMediaUrl(mediaReq.getMediaUrl());
                media.setMediaType(mediaReq.getMediaType() != null ? mediaReq.getMediaType() : "image");
                media.setThumbnailUrl(mediaReq.getThumbnailUrl());
                media.setOrderIndex(mediaReq.getOrderIndex() != null ? mediaReq.getOrderIndex() : 0);
                savedMediaList.add(postMediaRepository.save(media));
            }
        }

        PostResponseDto dto = mapToPostDto(savedPost);
        dto.setMedia(savedMediaList.stream().map(this::mapToMediaDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    public PostResponseDto getPostById(UUID postId) {
        System.out.println("Fetching post by ID: " + postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Post not found"));

        PostResponseDto dto = mapToPostDto(post);
        List<PostMedia> postMediaList = postMediaRepository.findByPostIdInOrderByOrderIndexAsc(List.of(postId));
        dto.setMedia(postMediaList.stream().map(this::mapToMediaDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        System.out.println("Deleting post: " + postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You do not have permission to delete this post");
        }

        postRepository.delete(post);
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
        if (post.getItinerary() != null) {
            PostResponseDto.ItineraryCompactDto itDto = new PostResponseDto.ItineraryCompactDto();
            itDto.setId(post.getItinerary().getId());
            itDto.setTitle(post.getItinerary().getTitle());
            itDto.setIsPublic(post.getItinerary().getIsPublic());
            itDto.setDescription(post.getItinerary().getDescription());
            dto.setItinerary(itDto);
        }
        if (post.getOriginalPost() != null) {
            PostResponseDto origDto = mapToPostDto(post.getOriginalPost());
            List<PostMedia> origMediaList = postMediaRepository.findByPostIdInOrderByOrderIndexAsc(List.of(post.getOriginalPost().getId()));
            origDto.setMedia(origMediaList.stream().map(this::mapToMediaDto).collect(Collectors.toList()));
            dto.setOriginalPost(origDto);
        }
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
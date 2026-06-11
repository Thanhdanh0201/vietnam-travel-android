package com.example.travel_backend.service.impl;

import com.example.travel_backend.dto.request.CreatePostRequestDto;
import com.example.travel_backend.dto.response.PostMediaDto;
import com.example.travel_backend.dto.response.PostResponseDto;
import com.example.travel_backend.dto.response.UserCompactDto;
import com.example.travel_backend.entity.Post;
import com.example.travel_backend.entity.PostMedia;
import com.example.travel_backend.entity.User;
import com.example.travel_backend.entity.SavedPost;
import com.example.travel_backend.repository.*;
import com.example.travel_backend.entity.Mention;
import com.example.travel_backend.service.NotificationTriggerService;
import com.example.travel_backend.service.PostService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Autowired
    private SavedPostRepository savedPostRepository;

    @Autowired
    private com.example.travel_backend.repository.MentionRepository mentionRepository;

    @Autowired
    private NotificationTriggerService notificationTriggerService;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_.]+)");


    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PostResponseDto> getUserPosts(UUID userId, int limit, int offset) {
        System.out.println("Fetching posts for user: " + userId + " | limit: " + limit + ", offset: " + offset);

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);

        Page<Post> postPage = postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return mapPostPageToDtoList(postPage.getContent());
    }

    // --- 4.1 Lấy Community Feed ---
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PostResponseDto> getPublicFeed(int limit, int offset) {
        System.out.println("Fetching public feed | limit: " + limit + ", offset: " + offset);
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Post> postPage = postRepository.findByVisibilityOrderByCreatedAtDesc("public", pageable);
        return mapPostPageToDtoList(postPage.getContent());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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

        if (request.getItineraryId() != null) {
            com.example.travel_backend.entity.Itinerary itinerary = itineraryRepository.findById(request.getItineraryId()).orElse(null);
            if (itinerary != null) {
                post.setItinerary(itinerary);
                itinerary.setIsPublic(true);
                itinerary.setShareCount((itinerary.getShareCount() != null ? itinerary.getShareCount() : 0) + 1);
                itineraryRepository.save(itinerary);
            }
        }
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

        // Refetch to ensure all lazy associations (user, place, province) are properly loaded
        final Post persistedPost = postRepository.findById(savedPost.getId()).orElseThrow();

        List<PostMedia> savedMediaList = new ArrayList<>();

        // Luu Media
        if (request.getMedia() != null && !request.getMedia().isEmpty()) {
            for (var mediaReq : request.getMedia()) {
                PostMedia media = new PostMedia();
                media.setPost(persistedPost);
                media.setMediaUrl(mediaReq.getMediaUrl());
                media.setMediaType(mediaReq.getMediaType() != null ? mediaReq.getMediaType() : "image");
                media.setThumbnailUrl(mediaReq.getThumbnailUrl());
                media.setOrderIndex(mediaReq.getOrderIndex() != null ? mediaReq.getOrderIndex() : 0);
                media.setCreatedAt(java.time.OffsetDateTime.now());
                savedMediaList.add(postMediaRepository.save(media));
            }
        }

        if (request.getContent() != null) {
            Set<String> mentionedUsernames = new HashSet<>();
            Matcher matcher = MENTION_PATTERN.matcher(request.getContent());
            while (matcher.find()) {
                mentionedUsernames.add(matcher.group(1));
            }
            for (String username : mentionedUsernames) {
                userRepository.findByUsernameIgnoreCase(username).ifPresent(mentionedUser -> {
                    Mention mention = new Mention();
                    mention.setMentionedUser(mentionedUser);
                    mention.setMentionedBy(userRepository.getReferenceById(userId));
                    mention.setPost(persistedPost);
                    mention.setCreatedAt(java.time.OffsetDateTime.now());
                    mentionRepository.save(mention);
                    notificationTriggerService.notifyMention(userId, mentionedUser.getId(), persistedPost.getId(), null);
                });
            }
        }

        PostResponseDto dto = mapToPostDto(persistedPost);
        dto.setMedia(savedMediaList.stream().map(this::mapToMediaDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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

    @Override
    @Transactional
    public void savePost(UUID userId, UUID postId) {
        if (savedPostRepository.findByUserIdAndPostId(userId, postId).isPresent()) {
            return;
        }
        SavedPost savedPost = new SavedPost();
        savedPost.setUser(userRepository.getReferenceById(userId));
        savedPost.setPost(postRepository.getReferenceById(postId));
        savedPost.setCreatedAt(java.time.OffsetDateTime.now());
        savedPostRepository.save(savedPost);
    }

    @Override
    @Transactional
    public void unsavePost(UUID userId, UUID postId) {
        savedPostRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PostResponseDto> getSavedPosts(UUID userId, int limit, int offset) {
        System.out.println("Fetching saved posts for user: " + userId + " | limit: " + limit + ", offset: " + offset);
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<SavedPost> savedPostPage = savedPostRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Post> posts = savedPostPage.getContent().stream()
                .map(SavedPost::getPost)
                .collect(Collectors.toList());
        return mapPostPageToDtoList(posts);
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
        if (post.getPlace() != null) {
            PostResponseDto.PlaceCompactDto placeDto = new PostResponseDto.PlaceCompactDto();
            placeDto.setId(post.getPlace().getId());
            placeDto.setName(post.getPlace().getName());
            placeDto.setLat(post.getPlace().getLat());
            placeDto.setLng(post.getPlace().getLng());
            placeDto.setImageUrl(post.getPlace().getImageUrl());
            if (post.getPlace().getProvince() != null) {
                placeDto.setProvinceName(post.getPlace().getProvince().getName());
            }
            dto.setPlace(placeDto);
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
package com.example.vietnam_travel_itinerary_android.data.dto

// User compact info
data class UserCompactDto(
    val id: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val explorerLevel: String? = null,
    val isVerified: Boolean? = null
)

// Post media info
data class PostMediaBackendDto(
    val id: String? = null,
    val mediaUrl: String? = null,
    val mediaType: String? = "image",
    val thumbnailUrl: String? = null,
    val orderIndex: Int? = 0
)

// Itinerary compact info
data class ItineraryCompactDto(
    val id: String,
    val title: String? = null,
    val isPublic: Boolean? = null,
    val description: String? = null
)

// Place compact info (returned inside PostResponseBackendDto)
data class PlaceCompactBackendDto(
    val id: String,
    val name: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val imageUrl: String? = null,
    val provinceName: String? = null
)

// Post response from Spring Boot backend
data class PostResponseBackendDto(
    val id: String,
    val content: String? = null,
    val postType: String? = null,
    val visibility: String? = null,
    val reactionCount: Int? = 0,
    val commentCount: Int? = 0,
    val repostCount: Int? = 0,
    val isEdited: Boolean? = false,
    val isPinned: Boolean? = false,
    val createdAt: String? = null, // ISO string from OffsetDateTime
    val author: UserCompactDto? = null,
    val media: List<PostMediaBackendDto>? = null,
    val itinerary: ItineraryCompactDto? = null,
    val place: PlaceCompactBackendDto? = null,
    val originalPost: PostResponseBackendDto? = null
)

// Request DTO for creating a post
data class CreatePostRequest(
    val content: String,
    val postType: String? = "text",
    val visibility: String? = "public",
    val itineraryId: String? = null,
    val placeId: String? = null,
    val media: List<PostMediaBackendDto>? = null
)

// Request DTO for post reaction (like)
data class ReactionRequest(
    val postId: String,
    val reactionType: String = "like"
)

// Request DTO for reposting
data class RepostRequest(
    val postId: String,
    val quoteText: String? = null
)

// Comment response from Spring Boot backend
data class CommentResponseBackendDto(
    val id: String,
    val postId: String,
    val parentCommentId: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val createdAt: String? = null, // ISO string
    val authorId: String? = null,
    val authorName: String? = null,
    val authorAvatarUrl: String? = null,
    val replyCount: Int? = 0,
    val likeCount: Int? = 0
)

// Request DTO for creating a comment
data class CommentRequest(
    val postId: String,
    val parentCommentId: String? = null,
    val content: String,
    val imageUrl: String? = null
)

// Request DTO for comment reaction (like)
data class CommentReactionRequest(
    val commentId: String,
    val reactionType: String = "like"
)

// Notification response from Spring Boot backend
data class NotificationResponseBackendDto(
    val id: String,
    val type: String? = null,
    val previewText: String? = null,
    val reactionType: String? = null,
    val isRead: Boolean? = false,
    val createdAt: String? = null, // ISO string
    val postId: String? = null,
    val commentId: String? = null,
    val achievementId: String? = null,
    val itineraryId: String? = null,
    val itineraryTitle: String? = null,
    val placeSuggestionId: String? = null,
    val actorId: String? = null,
    val actorName: String? = null,
    val actorUsername: String? = null,
    val actorAvatarUrl: String? = null,
    val groupKey: String? = null
)

data class UnreadCountDto(
    val count: Long = 0
)

// Request DTO for marking notifications as read
data class NotificationPatchDto(
    val isRead: Boolean
)

// Request DTO for report
data class ReportRequest(
    val reason: String,
    val reportedPostId: String? = null,
    val reportedCommentId: String? = null,
    val reportedUserId: String? = null,
    val description: String? = null
)

// User profile response from Spring Boot backend
data class UserProfileResponseDto(
    val id: String,
    val name: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val bio: String? = null,
    val username: String? = null,
    val websiteUrl: String? = null,
    val explorerLevel: String? = null,
    val totalProvinces: Int? = 0,
    val totalPlacesVisited: Int? = 0,
    val followerCount: Int? = 0,
    val followingCount: Int? = 0,
    val postCount: Int? = 0,
    val isVerified: Boolean? = false,
    val isPrivate: Boolean? = false,
    val role: String? = "user"
)

data class UpdateProfileRequest(
    val name: String? = null,
    val username: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val isPrivate: Boolean? = null
)

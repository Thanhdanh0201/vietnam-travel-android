package com.example.vietnam_travel_itinerary_android.data.model

// ── Tương ứng bảng: post_media
data class PostMedia(
    val id: String,
    val mediaUrl: String,           // URL thực hoặc key placeholder
    val mediaType: String = "image",// "image" | "video"
    val orderIndex: Int = 0
)

// ── Tương ứng bảng: posts (post_type = 'repost' | 'quote')
// Bài viết gốc được embed khi repost / quote
data class EmbeddedPost(
    val originalPostId: String,
    val originalAuthorName: String,
    val originalAuthorInitials: String,
    val originalAuthorColor: Long,
    val originalContent: String,
    val originalMedia: List<PostMedia> = emptyList(),
    val originalTimeAgo: String = ""
)

// ── Tương ứng bảng: posts
// post_type: "original" | "repost" | "quote"
data class CommunityPost(
    val id: String,
    val userId: String = "",
    val authorName: String,
    val authorAvatarInitials: String,
    val authorAvatarColor: Long,
    val timeAgo: String,
    val content: String,                        // Nội dung bài viết / caption khi quote
    val postType: String = "original",          // "original" | "repost" | "quote"
    val media: List<PostMedia> = emptyList(),   // post_media[]
    val likeCount: Int = 0,                     // post_reactions COUNT
    val commentCount: Int = 0,
    val repostCount: Int = 0,                   // reposts COUNT
    val isLiked: Boolean = false,
    val linkedItinerary: LinkedItinerary? = null,
    val embeddedPost: EmbeddedPost? = null,     // Bài gốc nếu là repost/quote
    val comments: List<Comment> = emptyList()   // Mock comments (thực tế load lazy)
)

data class LinkedItinerary(
    val id: String,
    val title: String,
    val stopCount: Int
)

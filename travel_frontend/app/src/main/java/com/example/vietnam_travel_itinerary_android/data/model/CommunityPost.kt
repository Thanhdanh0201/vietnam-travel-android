package com.example.vietnam_travel_itinerary_android.data.model

import androidx.compose.runtime.Immutable

// ── Tương ứng bảng: post_media
@Immutable
data class PostMedia(
    val id: String,
    val mediaUrl: String,           // URL thực hoặc key placeholder
    val mediaType: String = "image",// "image" | "video"
    val orderIndex: Int = 0
)

// ── Tương ứng bảng: posts (post_type = 'repost' | 'quote')
// Bài viết gốc được embed khi repost / quote
@Immutable
data class EmbeddedPost(
    val originalPostId: String,
    val originalAuthorName: String,
    val originalAuthorInitials: String,
    val originalAuthorColor: Long,
    val originalAuthorAvatarUrl: String = "",
    val originalContent: String,
    val originalMedia: List<PostMedia> = emptyList(),
    val originalTimeAgo: String = ""
)

// ── Địa điểm đính kèm bài viết
@Immutable
data class PostPlace(
    val id: String,
    val name: String,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val imageUrl: String = "",
    val provinceName: String = ""
)

// ── Tương ứng bảng: posts
// post_type: "original" | "repost" | "quote"
@Immutable
data class CommunityPost(
    val id: String,
    val userId: String = "",
    val authorName: String,
    val authorAvatarUrl: String = "",
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
    val place: PostPlace? = null,               // Địa điểm đính kèm
    val linkedItinerary: LinkedItinerary? = null,
    val embeddedPost: EmbeddedPost? = null,     // Bài gốc nếu là repost/quote
    val comments: List<Comment> = emptyList()   // Mock comments (thực tế load lazy)
)

// ── Tương ứng bảng: itineraries (fields dùng cho hiển thị)
// - Compact mode: nhúng trong bài viết (PostCard → LinkedItineraryCard)
// - Full mode: hiển thị trong Profile tab "Lịch trình" & ItineraryScreen
@Immutable
data class LinkedItinerary(
    val id: String,
    val title: String,
    val stopCount: Int,
    // ── Extended fields cho Profile tab
    val location: String = "",              // Địa điểm chính (tỉnh/thành)
    val durationDays: Int = 0,             // Số ngày
    val isPublic: Boolean = true,          // Có công khai không
    val likeCount: Int = 0,
    val coverImageKey: String = "",        // key ảnh bìa (dùng với ImagePlaceholderBox)
    val authorName: String = "",
    val authorAvatarInitials: String = "",
    val authorAvatarColor: Long = 0xFF64748B,
    val timeAgo: String = ""
)

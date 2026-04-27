package com.example.vietnam_travel_itinerary_android.data.model

// ── Tương ứng bảng: comment_reactions
data class CommentReaction(
    val userId: String,
    val reactionType: String = "like" // "like" | "love" | "haha"
)

// ── Tương ứng bảng: comments
// Hỗ trợ nested: parentCommentId != null => đây là reply
data class Comment(
    val id: String,
    val postId: String,
    val userId: String = "",
    val parentCommentId: String? = null,   // null => top-level, có id => reply
    val authorName: String,
    val authorAvatarInitials: String,
    val authorAvatarColor: Long,
    val timeAgo: String,
    val content: String,
    val reactionCount: Int = 0,
    val replyCount: Int = 0,
    val isLiked: Boolean = false,
    val replies: List<Comment> = emptyList() // Mock nested replies
)

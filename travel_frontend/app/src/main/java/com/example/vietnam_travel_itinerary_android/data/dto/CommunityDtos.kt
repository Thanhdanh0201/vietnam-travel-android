package com.example.vietnam_travel_itinerary_android.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val avatar_url: String? = null,
    val explorer_level: String? = "newbie",
    val is_verified: Boolean = false
)

@Serializable
data class PostMediaDto(
    val id: String? = null,
    val post_id: String? = null,
    val media_url: String,
    val media_type: String = "image",
    val order_index: Int = 0,
    val thumbnail_url: String? = null
)

@Serializable
data class ItineraryDto(
    val id: String,
    val title: String,
    val is_public: Boolean = true,
    val description: String? = null
)

@Serializable
data class PostDto(
    val id: String,
    val user_id: String? = null,
    val content: String? = null,
    val post_type: String = "text",
    val visibility: String = "public",
    val itinerary_id: String? = null,
    val place_id: String? = null,
    val province_id: String? = null,
    val reaction_count: Int = 0,
    val comment_count: Int = 0,
    val repost_count: Int = 0,
    val created_at: String,
    
    // View joined fields
    val author_name: String? = null,
    val author_avatar_url: String? = null,
    val author_explorer_level: String? = null,
    val author_is_verified: Boolean? = null,
    
    // Embedded post fields (for reposts/quotes)
    val original_post_id: String? = null,
    val original_content: String? = null,
    val original_author_name: String? = null,
    val original_author_avatar_url: String? = null,
    val original_author_explorer_level: String? = null,
    val original_author_is_verified: Boolean? = null,
    val original_created_at: String? = null,
    
    // Relations when querying table directly
    val post_media: List<PostMediaDto> = emptyList(),
    val user: UserDto? = null,
    val itinerary: ItineraryDto? = null
)

@Serializable
data class CommentDto(
    val id: String,
    val post_id: String,
    val user_id: String,
    val parent_comment_id: String? = null,
    val content: String,
    val reaction_count: Int = 0,
    val reply_count: Int = 0,
    val created_at: String,
    
    // View joined fields
    val author_name: String? = null,
    val author_avatar_url: String? = null,
    val author_explorer_level: String? = null,
    
    // Relation when querying table
    val user: UserDto? = null
)

@Serializable
data class NotificationDto(
    val id: String,
    val user_id: String,
    val actor_id: String? = null,
    val notif_type: String, // "like" | "comment" | "follow" | "repost" | "mention"
    val post_id: String? = null,
    val comment_id: String? = null,
    val is_read: Boolean = false,
    val created_at: String,
    
    // Relation when querying table
    val actor: UserDto? = null
)

@Serializable
data class PostReactionDto(
    val id: String? = null,
    val post_id: String,
    val user_id: String,
    val reaction_type: String = "like"
)

@Serializable
data class CommentReactionDto(
    val id: String? = null,
    val comment_id: String,
    val user_id: String,
    val reaction_type: String = "like"
)

@Serializable
data class RepostDto(
    val id: String? = null,
    val user_id: String,
    val post_id: String,
    val quote_text: String? = null,
    val created_at: String? = null
)

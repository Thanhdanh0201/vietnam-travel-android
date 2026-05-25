package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.*
import com.example.vietnam_travel_itinerary_android.data.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

class CommunityRepository(private val supabase: SupabaseClient) {

    private val api = RetrofitInstance.api

    // Helper functions for avatars
    private fun getAvatarColor(name: String): Long {
        val colors = listOf(
            0xFF2563EB, // Blue
            0xFF7C3AED, // Purple
            0xFF059669, // Green
            0xFFEA580C, // Orange
            0xFFDB2777, // Pink
            0xFF0891B2, // Cyan
            0xFFC6102E  // Red
        )
        val index = Math.abs(name.hashCode()) % colors.size
        return colors[index]
    }

    private fun getInitials(name: String): String {
        if (name.isBlank()) return "U"
        val parts = name.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return "U"
        if (parts.size == 1) return parts[0].take(2).uppercase()
        val first = parts.first().take(1)
        val last = parts.last().take(1)
        return (first + last).uppercase()
    }

    private fun formatTimeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "vừa xong"
        return try {
            val parsed = OffsetDateTime.parse(isoString)
            val now = OffsetDateTime.now(parsed.offset)
            val diff = Duration.between(parsed, now)
            val seconds = diff.seconds
            when {
                seconds < 6 -> "vừa xong"
                seconds < 60 -> "$seconds giây trước"
                seconds < 3600 -> "${seconds / 60} phút trước"
                seconds < 86400 -> "${seconds / 3600} giờ trước"
                seconds < 604800 -> "${seconds / 86400} ngày trước"
                else -> parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
        } catch (e: Exception) {
            "vừa xong"
        }
    }

    // Mapping DTOs to UI models
    private fun PostResponseBackendDto.toCommunityPost(currentUserId: String? = null, likedPostIds: Set<String> = emptySet()): CommunityPost {
        val authorNameVal = author?.name ?: "Người dùng"
        val isLikedVal = likedPostIds.contains(id)
        val postTypeVal = postType ?: "text"
        
        val embeddedVal = originalPost?.let { orig ->
            EmbeddedPost(
                originalPostId = orig.id,
                originalAuthorName = orig.author?.name ?: "Người dùng",
                originalAuthorInitials = getInitials(orig.author?.name ?: "Người dùng"),
                originalAuthorColor = getAvatarColor(orig.author?.name ?: "Người dùng"),
                originalContent = orig.content ?: "",
                originalMedia = orig.media?.map {
                    PostMedia(
                        id = it.id ?: "",
                        mediaUrl = it.mediaUrl ?: "",
                        mediaType = it.mediaType ?: "image",
                        orderIndex = it.orderIndex ?: 0
                    )
                } ?: emptyList(),
                originalTimeAgo = formatTimeAgo(orig.createdAt)
            )
        }

        val mediaList = media?.map {
            PostMedia(
                id = it.id ?: "",
                mediaUrl = it.mediaUrl ?: "",
                mediaType = it.mediaType ?: "image",
                orderIndex = it.orderIndex ?: 0
            )
        } ?: emptyList()

        val linkedItineraryVal = itinerary?.let {
            LinkedItinerary(
                id = it.id,
                title = it.title ?: "",
                stopCount = 0,
                isPublic = it.isPublic ?: true
            )
        }

        return CommunityPost(
            id = id,
            userId = author?.id ?: "",
            authorName = authorNameVal,
            authorAvatarInitials = getInitials(authorNameVal),
            authorAvatarColor = getAvatarColor(authorNameVal),
            timeAgo = formatTimeAgo(createdAt).uppercase(),
            content = content ?: "",
            postType = if (postTypeVal == "text" || postTypeVal == "image") "original" else postTypeVal,
            media = mediaList,
            likeCount = reactionCount ?: 0,
            commentCount = commentCount ?: 0,
            repostCount = repostCount ?: 0,
            isLiked = isLikedVal,
            linkedItinerary = linkedItineraryVal,
            embeddedPost = embeddedVal
        )
    }

    private fun CommentResponseBackendDto.toComment(likedCommentIds: Set<String> = emptySet(), repliesList: List<Comment> = emptyList()): Comment {
        val authorNameVal = authorName ?: "Người dùng"
        return Comment(
            id = id,
            postId = postId,
            userId = authorId ?: "",
            parentCommentId = parentCommentId,
            authorName = authorNameVal,
            authorAvatarInitials = getInitials(authorNameVal),
            authorAvatarColor = getAvatarColor(authorNameVal),
            timeAgo = formatTimeAgo(createdAt),
            content = content ?: "",
            reactionCount = likeCount ?: 0,
            replyCount = replyCount ?: 0,
            isLiked = likedCommentIds.contains(id),
            replies = repliesList
        )
    }

    private fun NotificationResponseBackendDto.toNotificationDto(): NotificationDto {
        return NotificationDto(
            id = id,
            user_id = "",
            actor_id = actorId,
            notif_type = type?.lowercase() ?: "like",
            post_id = postId,
            comment_id = commentId,
            is_read = isRead ?: false,
            created_at = createdAt ?: "",
            actor = actorName?.let { name ->
                UserDto(
                    id = actorId ?: "",
                    name = name,
                    avatar_url = actorAvatarUrl
                )
            }
        )
    }

    // --- 4.1 Lấy Community Feed ---
    suspend fun getPublicFeed(currentUserId: String? = null, limit: Int = 20, offset: Int = 0): List<CommunityPost> = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            val postsDto = api.getPublicFeed(limit, offset, token)

            val likedPostIds = if (token?.isNotBlank() == true && postsDto.isNotEmpty()) {
                try {
                    api.checkLikedPosts(token, postsDto.map { it.id }).toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else emptySet()

            postsDto.map { it.toCommunityPost(currentUserId, likedPostIds) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getFollowingFeed(currentUserId: String, limit: Int = 20, offset: Int = 0): List<CommunityPost> = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext emptyList()
            val postsDto = api.getFollowingFeed(limit, offset, token)

            val likedPostIds = if (postsDto.isNotEmpty()) {
                try {
                    api.checkLikedPosts(token, postsDto.map { it.id }).toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else emptySet()

            postsDto.map { it.toCommunityPost(currentUserId, likedPostIds) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getPostDetails(postId: String, currentUserId: String? = null): CommunityPost? = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            val postDto = api.getPostDetails(postId, token)

            val likedPostIds = if (token?.isNotBlank() == true) {
                try {
                    api.checkLikedPosts(token, listOf(postId)).toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else emptySet()

            postDto.toCommunityPost(currentUserId, likedPostIds)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // --- 4.2 Tạo bài đăng mới ---
    suspend fun createPost(
        userId: String,
        content: String,
        postType: String = "text",
        visibility: String = "public",
        itineraryId: String? = null,
        placeId: String? = null,
        provinceId: String? = null,
        mediaUrls: List<String> = emptyList()
    ): CommunityPost = withContext(Dispatchers.IO) {
        val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: throw Exception("Not authenticated")

        val mediaDtos = mediaUrls.mapIndexed { index, url ->
            PostMediaBackendDto(
                mediaUrl = url,
                mediaType = "image",
                orderIndex = index
            )
        }

        val request = CreatePostRequest(
            content = content,
            postType = postType,
            visibility = visibility,
            itineraryId = itineraryId,
            placeId = placeId,
            media = mediaDtos
        )

        val createdPost = api.createPost(token, request)
        createdPost.toCommunityPost(userId)
    }

    // --- 4.3 Repost / Quote ---
    suspend fun repostPost(userId: String, postId: String, quoteText: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val request = RepostRequest(postId = postId, quoteText = quoteText)
            val response = api.repostPost(token, request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun undoRepost(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val response = api.undoRepost(token, postId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.4 Like / Unlike bài đăng ---
    suspend fun likePost(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val request = ReactionRequest(postId = postId)
            val response = api.likePost(token, request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unlikePost(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val response = api.unlikePost(token, postId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.5 Xoá bài đăng ---
    suspend fun deletePost(postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val response = api.deletePost(token, postId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.6 Lấy bình luận ---
    suspend fun getComments(postId: String, currentUserId: String? = null): List<Comment> = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            val topLevelDtos = api.getComments(postId = postId, limit = 100)
            val allReplies = mutableListOf<CommentResponseBackendDto>()

            for (topComment in topLevelDtos) {
                if ((topComment.replyCount ?: 0) > 0) {
                    try {
                        val replies = api.getComments(parentCommentId = topComment.id)
                        allReplies.addAll(replies)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val allCommentIds = (topLevelDtos.map { it.id } + allReplies.map { it.id })
            val likedCommentIds = if (token != null && allCommentIds.isNotEmpty()) {
                try {
                    api.checkLikedComments(token, allCommentIds).toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else emptySet()

            val repliesMapped = allReplies.map { it.toComment(likedCommentIds) }
            val repliesByParent = repliesMapped.groupBy { it.parentCommentId }

            topLevelDtos.map { commentDto ->
                val replies = repliesByParent[commentDto.id] ?: emptyList()
                commentDto.toComment(likedCommentIds, replies)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // --- 4.7 Đăng bình luận/trả lời ---
    suspend fun postComment(
        postId: String,
        userId: String,
        content: String,
        parentCommentId: String? = null
    ): Comment = withContext(Dispatchers.IO) {
        val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: throw Exception("Not authenticated")
        val request = CommentRequest(postId = postId, parentCommentId = parentCommentId, content = content)
        val createdDto = api.postComment(token, request)
        createdDto.toComment()
    }

    // --- 4.8 Like / Unlike bình luận ---
    suspend fun likeComment(commentId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val request = CommentReactionRequest(commentId = commentId)
            val response = api.likeComment(token, request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unlikeComment(commentId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val response = api.unlikeComment(token, commentId)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.9 Report bài đăng / bình luận ---
    suspend fun report(
        userId: String,
        reason: String,
        reportedPostId: String?,
        reportedCommentId: String?,
        description: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val request = ReportRequest(
                reason = reason,
                reportedPostId = reportedPostId,
                reportedCommentId = reportedCommentId,
                description = description
            )
            val response = api.report(token, request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.10 Lấy Notifications ---
    suspend fun getNotifications(userId: String, limit: Int = 30, offset: Int = 0): List<NotificationDto> = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext emptyList()
            val backendNotifs = api.getNotifications(token, limit, offset)
            backendNotifs.map { it.toNotificationDto() }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun markNotificationsAsRead(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" } ?: return@withContext false
            val request = NotificationPatchDto(isRead = true)
            val response = api.markNotificationsAsRead(token, request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 6. Storage Configuration (Upload file) ---
    suspend fun uploadMedia(byteArray: ByteArray, fileName: String): String = withContext(Dispatchers.IO) {
        val bucket = supabase.storage["post-media"]
        val path = "${System.currentTimeMillis()}_$fileName"
        bucket.upload(path, byteArray) {
            upsert = true
        }
        return@withContext bucket.publicUrl(path)
    }
}

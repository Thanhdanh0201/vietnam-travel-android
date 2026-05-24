package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.dto.*
import com.example.vietnam_travel_itinerary_android.data.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

class CommunityRepository(private val supabase: SupabaseClient) {

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

    private fun formatTimeAgo(isoString: String): String {
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
    private fun PostDto.toCommunityPost(currentUserId: String? = null, likedPostIds: Set<String> = emptySet()): CommunityPost {
        val authorNameVal = author_name ?: user?.name ?: "Người dùng"
        val isLikedVal = likedPostIds.contains(id)
        val postTypeVal = post_type
        
        val embeddedVal = if (original_post_id != null) {
            EmbeddedPost(
                originalPostId = original_post_id,
                originalAuthorName = original_author_name ?: "Người dùng",
                originalAuthorInitials = getInitials(original_author_name ?: "Người dùng"),
                originalAuthorColor = getAvatarColor(original_author_name ?: "Người dùng"),
                originalContent = original_content ?: "",
                originalMedia = emptyList(),
                originalTimeAgo = original_created_at?.let { formatTimeAgo(it) } ?: ""
            )
        } else null

        val mediaList = post_media.map {
            PostMedia(
                id = it.id ?: "",
                mediaUrl = it.media_url,
                mediaType = it.media_type,
                orderIndex = it.order_index
            )
        }

        val linkedItineraryVal = itinerary?.let {
            LinkedItinerary(
                id = it.id,
                title = it.title,
                stopCount = 0,
                isPublic = it.is_public
            )
        }

        return CommunityPost(
            id = id,
            userId = user_id ?: user?.id ?: "",
            authorName = authorNameVal,
            authorAvatarInitials = getInitials(authorNameVal),
            authorAvatarColor = getAvatarColor(authorNameVal),
            timeAgo = formatTimeAgo(created_at).uppercase(),
            content = content ?: "",
            postType = if (postTypeVal == "text" || postTypeVal == "image") "original" else postTypeVal,
            media = mediaList,
            likeCount = reaction_count,
            commentCount = comment_count,
            repostCount = repost_count,
            isLiked = isLikedVal,
            linkedItinerary = linkedItineraryVal,
            embeddedPost = embeddedVal
        )
    }

    private fun CommentDto.toComment(likedCommentIds: Set<String> = emptySet(), repliesList: List<Comment> = emptyList()): Comment {
        val authorNameVal = author_name ?: user?.name ?: "Người dùng"
        return Comment(
            id = id,
            postId = post_id,
            userId = user_id,
            parentCommentId = parent_comment_id,
            authorName = authorNameVal,
            authorAvatarInitials = getInitials(authorNameVal),
            authorAvatarColor = getAvatarColor(authorNameVal),
            timeAgo = formatTimeAgo(created_at),
            content = content,
            reactionCount = reaction_count,
            replyCount = reply_count,
            isLiked = likedCommentIds.contains(id),
            replies = repliesList
        )
    }

    // --- 4.1 Lấy Community Feed ---
    suspend fun getPublicFeed(currentUserId: String? = null, limit: Int = 20, offset: Int = 0): List<CommunityPost> = withContext(Dispatchers.IO) {
        val postsDto = supabase.postgrest["posts"].select(
            columns = Columns.raw("""
                *,
                post_media(media_url, media_type, order_index, thumbnail_url),
                user:users(id, name, avatar_url, is_verified, explorer_level),
                itinerary:itineraries(id, title, is_public, description)
            """.trimIndent())
        ) {
            filter {
                eq("visibility", "public")
            }
            order("created_at", Order.DESCENDING)
            range(offset.toLong()..(offset + limit - 1).toLong())
        }.decodeList<PostDto>()

        val likedPostIds = if (currentUserId != null && postsDto.isNotEmpty()) {
            val ids = postsDto.map { it.id }
            supabase.postgrest["post_reactions"].select(Columns.raw("post_id")) {
                filter {
                    eq("user_id", currentUserId)
                    isIn("post_id", ids)
                }
            }.decodeList<PostReactionDto>().map { it.post_id }.toSet()
        } else emptySet()

        return@withContext postsDto.map { it.toCommunityPost(currentUserId, likedPostIds) }
    }

    suspend fun getFollowingFeed(currentUserId: String, limit: Int = 20, offset: Int = 0): List<CommunityPost> = withContext(Dispatchers.IO) {
        // First get the list of user IDs being followed
        val followingUsers = supabase.postgrest["follows"].select(Columns.raw("following_id")) {
            filter {
                eq("follower_id", currentUserId)
            }
        }.decodeList<UserSyncRequest>() // Use a simple container or map
        
        val followingIds = followingUsers.map { it.id } // wait, user profile sync has id
        if (followingIds.isEmpty()) return@withContext emptyList()

        val postsDto = supabase.postgrest["posts"].select(
            columns = Columns.raw("""
                *,
                post_media(media_url, media_type, order_index, thumbnail_url),
                user:users(id, name, avatar_url, is_verified, explorer_level),
                itinerary:itineraries(id, title, is_public, description)
            """.trimIndent())
        ) {
            filter {
                isIn("user_id", followingIds)
            }
            order("created_at", Order.DESCENDING)
            range(offset.toLong()..(offset + limit - 1).toLong())
        }.decodeList<PostDto>()

        val likedPostIds = if (postsDto.isNotEmpty()) {
            val ids = postsDto.map { it.id }
            supabase.postgrest["post_reactions"].select(Columns.raw("post_id")) {
                filter {
                    eq("user_id", currentUserId)
                    isIn("post_id", ids)
                }
            }.decodeList<PostReactionDto>().map { it.post_id }.toSet()
        } else emptySet()

        return@withContext postsDto.map { it.toCommunityPost(currentUserId, likedPostIds) }
    }

    suspend fun getPostDetails(postId: String, currentUserId: String? = null): CommunityPost? = withContext(Dispatchers.IO) {
        try {
            val postsDto = supabase.postgrest["posts"].select(
                columns = Columns.raw("""
                    *,
                    post_media(media_url, media_type, order_index, thumbnail_url),
                    user:users(id, name, avatar_url, is_verified, explorer_level),
                    itinerary:itineraries(id, title, is_public, description)
                """.trimIndent())
            ) {
                filter {
                    eq("id", postId)
                }
            }.decodeList<PostDto>()
            
            if (postsDto.isEmpty()) return@withContext null

            val likedPostIds = if (currentUserId != null) {
                supabase.postgrest["post_reactions"].select(Columns.raw("post_id")) {
                    filter {
                        eq("user_id", currentUserId)
                        eq("post_id", postId)
                    }
                }.decodeList<PostReactionDto>().map { it.post_id }.toSet()
            } else emptySet()

            return@withContext postsDto.first().toCommunityPost(currentUserId, likedPostIds)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        val postInsert = mapOf(
            "user_id" to userId,
            "content" to content,
            "post_type" to postType,
            "visibility" to visibility,
            "itinerary_id" to itineraryId,
            "place_id" to placeId,
            "province_id" to provinceId
        )

        // Insert post
        val createdPost = supabase.postgrest["posts"].insert(postInsert) {
            select()
        }.decodeSingle<PostDto>()

        // Insert media if present
        if (mediaUrls.isNotEmpty()) {
            val mediaDtos = mediaUrls.mapIndexed { index, url ->
                PostMediaDto(
                    post_id = createdPost.id,
                    media_url = url,
                    media_type = "image",
                    order_index = index
                )
            }
            supabase.postgrest["post_media"].insert(mediaDtos)
        }

        // Fetch complete post with joined fields to return to caller
        val posts = supabase.postgrest["posts"].select(
            columns = Columns.raw("""
                *,
                post_media(media_url, media_type, order_index, thumbnail_url),
                user:users(id, name, avatar_url, is_verified, explorer_level),
                itinerary:itineraries(id, title, is_public, description)
            """.trimIndent())
        ) {
            filter {
                eq("id", createdPost.id)
            }
        }.decodeList<PostDto>()

        return@withContext posts.first().toCommunityPost(userId)
    }

    // --- 4.3 Repost / Quote ---
    suspend fun repostPost(userId: String, postId: String, quoteText: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val repostInsert = mapOf(
                "user_id" to userId,
                "post_id" to postId,
                "quote_text" to quoteText
            )
            supabase.postgrest["reposts"].insert(repostInsert)
            
            // Note: Section 4.3 also mentions a quote is a quote text repost. 
            // In case we want to create a post of type "repost"/"quote" so it shows up in posts table:
            val postType = if (quoteText != null) "quote" else "repost"
            val postInsert = mapOf(
                "user_id" to userId,
                "content" to quoteText,
                "post_type" to postType,
                "visibility" to "public",
                "original_post_id" to postId
            )
            supabase.postgrest["posts"].insert(postInsert)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun undoRepost(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["reposts"].delete {
                filter {
                    eq("user_id", userId)
                    eq("post_id", postId)
                }
            }
            // Delete matching quote/repost post in posts table
            supabase.postgrest["posts"].delete {
                filter {
                    eq("user_id", userId)
                    eq("original_post_id", postId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.4 Like / Unlike bài đăng ---
    suspend fun likePost(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val reaction = mapOf(
                "post_id" to postId,
                "user_id" to userId,
                "reaction_type" to "like"
            )
            supabase.postgrest["post_reactions"].insert(reaction)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unlikePost(userId: String, postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["post_reactions"].delete {
                filter {
                    eq("post_id", postId)
                    eq("user_id", userId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.5 Xoá bài đăng ---
    suspend fun deletePost(postId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["posts"].delete {
                filter {
                    eq("id", postId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.6 Lấy bình luận ---
    suspend fun getComments(postId: String, currentUserId: String? = null): List<Comment> = withContext(Dispatchers.IO) {
        // Fetch top-level comments
        val commentDtos = supabase.postgrest["comments"].select(
            columns = Columns.raw("""
                *,
                user:users(id, name, avatar_url)
            """.trimIndent())
        ) {
            filter {
                eq("post_id", postId)
                exact("parent_comment_id", null)
            }
            order("created_at", Order.ASCENDING)
        }.decodeList<CommentDto>()

        // Fetch all replies
        val replyDtos = supabase.postgrest["comments"].select(
            columns = Columns.raw("""
                *,
                user:users(id, name, avatar_url)
            """.trimIndent())
        ) {
            filter {
                eq("post_id", postId)
                filterNot("parent_comment_id", FilterOperator.IS, null)
            }
            order("created_at", Order.ASCENDING)
        }.decodeList<CommentDto>()

        // Find which comments/replies the current user liked
        val allCommentIds = (commentDtos.map { it.id } + replyDtos.map { it.id })
        val likedCommentIds = if (currentUserId != null && allCommentIds.isNotEmpty()) {
            supabase.postgrest["comment_reactions"].select(Columns.raw("comment_id")) {
                filter {
                    eq("user_id", currentUserId)
                    isIn("comment_id", allCommentIds)
                }
            }.decodeList<CommentReactionDto>().map { it.comment_id }.toSet()
        } else emptySet()

        val repliesMapped = replyDtos.map { it.toComment(likedCommentIds) }
        val repliesByParent = repliesMapped.groupBy { it.parentCommentId }

        return@withContext commentDtos.map { commentDto ->
            val replies = repliesByParent[commentDto.id] ?: emptyList()
            commentDto.toComment(likedCommentIds, replies)
        }
    }

    // --- 4.7 Đăng bình luận/trả lời ---
    suspend fun postComment(
        postId: String,
        userId: String,
        content: String,
        parentCommentId: String? = null
    ): Comment = withContext(Dispatchers.IO) {
        val commentInsert = mapOf(
            "post_id" to postId,
            "user_id" to userId,
            "parent_comment_id" to parentCommentId,
            "content" to content
        )

        val inserted = supabase.postgrest["comments"].insert(commentInsert) {
            select()
        }.decodeSingle<CommentDto>()

        // Fetch with joined user details
        val detailed = supabase.postgrest["comments"].select(
            columns = Columns.raw("""
                *,
                user:users(id, name, avatar_url)
            """.trimIndent())
        ) {
            filter {
                eq("id", inserted.id)
            }
        }.decodeSingle<CommentDto>()

        return@withContext detailed.toComment()
    }

    // --- 4.8 Like / Unlike bình luận ---
    suspend fun likeComment(commentId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val reaction = mapOf(
                "comment_id" to commentId,
                "user_id" to userId,
                "reaction_type" to "like"
            )
            supabase.postgrest["comment_reactions"].insert(reaction)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unlikeComment(commentId: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["comment_reactions"].delete {
                filter {
                    eq("comment_id", commentId)
                    eq("user_id", userId)
                }
            }
            true
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
            val reportMap = mutableMapOf<String, Any?>(
                "reporter_id" to userId,
                "reason" to reason,
                "description" to description
            )
            if (reportedPostId != null) reportMap["reported_post_id"] = reportedPostId
            if (reportedCommentId != null) reportMap["reported_comment_id"] = reportedCommentId
            
            supabase.postgrest["reports"].insert(reportMap)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- 4.10 Lấy Notifications ---
    suspend fun getNotifications(userId: String, limit: Int = 30, offset: Int = 0): List<NotificationDto> = withContext(Dispatchers.IO) {
        try {
            return@withContext supabase.postgrest["notifications"].select(
                columns = Columns.raw("""
                    *,
                    actor:users(id, name, avatar_url)
                """.trimIndent())
            ) {
                filter {
                    eq("user_id", userId)
                }
                order("created_at", Order.DESCENDING)
                range(offset.toLong()..(offset + limit - 1).toLong())
            }.decodeList<NotificationDto>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun markNotificationsAsRead(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val updateData = mapOf("is_read" to true)
            supabase.postgrest["notifications"].update(updateData) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
            true
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

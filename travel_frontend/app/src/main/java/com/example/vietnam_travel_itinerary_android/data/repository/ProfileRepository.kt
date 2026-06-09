package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.ItineraryResponseDto
import com.example.vietnam_travel_itinerary_android.data.dto.PostResponseBackendDto
import com.example.vietnam_travel_itinerary_android.data.dto.UpdateProfileRequest
import com.example.vietnam_travel_itinerary_android.data.dto.UserProfileResponseDto
import com.example.vietnam_travel_itinerary_android.data.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ProfileRepository(
    private val supabase: SupabaseClient,
    private val communityRepository: CommunityRepository = CommunityRepository(supabase),
) {
    private val api = RetrofitInstance.api

    suspend fun getProfile(userId: String, currentUserId: String?): UserProfile = withContext(Dispatchers.IO) {
        val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
        val profileDto = fetchProfileDto(userId, token)
        val isOwnProfile = currentUserId != null && userId == currentUserId

        val isFollowing = if (!isOwnProfile && token != null && currentUserId != null) {
            try {
                api.checkIsFollowing(token, userId)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else false

        val posts = fetchUserPosts(userId, currentUserId, token)
        val displayName = profileDto.name ?: "Người dùng"
        val itineraries = fetchUserItineraries(
            userId = userId,
            isOwnProfile = isOwnProfile,
            token = token,
            authorName = displayName,
            authorInitials = getInitials(displayName),
            authorAvatarColor = getAvatarColor(displayName),
        )
        profileDto.toUserProfile(
            currentUserId = currentUserId,
            isFollowing = isFollowing,
            posts = posts,
            publicItineraries = itineraries,
        )
    }

    suspend fun followUser(targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
                ?: return@withContext false
            api.followUser(token, targetUserId).isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun unfollowUser(targetUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
                ?: return@withContext false
            api.unfollowUser(token, targetUserId).isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        avatarUrl: String? = null,
        coverUrl: String? = null,
        isPrivate: Boolean? = null,
    ): UserProfile = withContext(Dispatchers.IO) {
        val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            ?: throw IllegalStateException("Not authenticated")
        val currentUserId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not authenticated")

        val request = UpdateProfileRequest(
            name = name,
            username = username,
            bio = bio,
            avatarUrl = avatarUrl,
            coverUrl = coverUrl,
            isPrivate = isPrivate,
        )
        val dto = try {
            api.updateProfile(token, request)
        } catch (e: HttpException) {
            if (e.code() == 400) {
                throw IllegalArgumentException("Username đã được sử dụng hoặc không hợp lệ")
            }
            throw e
        }
        dto.toUserProfile(currentUserId = currentUserId, isFollowing = false, posts = emptyList())
    }

    suspend fun uploadAvatar(bytes: ByteArray, userId: String): String = withContext(Dispatchers.IO) {
        val bucket = supabase.storage["avatars"]
        val path = "$userId/${System.currentTimeMillis()}.jpg"
        bucket.upload(path, bytes) {
            upsert = true
        }
        bucket.publicUrl(path)
    }

    suspend fun likePost(userId: String, postId: String): Boolean =
        communityRepository.likePost(userId, postId)

    suspend fun unlikePost(userId: String, postId: String): Boolean =
        communityRepository.unlikePost(userId, postId)

    private suspend fun fetchProfileDto(userId: String, token: String?): UserProfileResponseDto {
        try {
            return api.getProfile(userId, token)
        } catch (e: Exception) {
            e.printStackTrace()
            if (token == null) throw e

            val user = supabase.auth.currentUserOrNull()
            if (user != null && user.id == userId) {
                val syncRequest = UserSyncRequest(
                    id = user.id,
                    email = user.email ?: "",
                    name = user.email?.substringBefore("@") ?: "Traveler",
                )
                val syncResponse = api.syncUser(token, syncRequest)
                if (syncResponse.isSuccessful) {
                    return api.getProfile(userId, token)
                }
            }
            throw e
        }
    }

    private suspend fun fetchUserPosts(
        userId: String,
        currentUserId: String?,
        token: String?,
    ): List<CommunityPost> {
        return try {
            val postsDto = api.getUserPosts(userId, limit = 50, offset = 0, token)
            val likedPostIds = if (token != null && postsDto.isNotEmpty()) {
                try {
                    api.checkLikedPosts(token, postsDto.map { it.id }).toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else emptySet()

            postsDto.map { it.toCommunityPost(currentUserId, likedPostIds) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun fetchUserItineraries(
        userId: String,
        isOwnProfile: Boolean,
        token: String?,
        authorName: String,
        authorInitials: String,
        authorAvatarColor: Long,
    ): List<LinkedItinerary> {
        if (token.isNullOrBlank()) return emptyList()
        return try {
            val dtos = if (isOwnProfile) {
                api.getMyItineraries(token)
            } else {
                api.getPublicItinerariesByUser(token, userId)
            }
            dtos.map {
                it.toLinkedItinerary(
                    authorName = authorName,
                    authorInitials = authorInitials,
                    authorAvatarColor = authorAvatarColor,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun ItineraryResponseDto.toLinkedItinerary(
        authorName: String,
        authorInitials: String,
        authorAvatarColor: Long,
    ): LinkedItinerary {
        val durationDays = if (!startDate.isNullOrBlank() && !endDate.isNullOrBlank()) {
            try {
                val start = java.time.LocalDate.parse(startDate)
                val end = java.time.LocalDate.parse(endDate)
                java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(1)
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }

        return LinkedItinerary(
            id = id,
            title = title,
            stopCount = itemCount ?: 0,
            location = location ?: "",
            durationDays = durationDays,
            isPublic = isPublic ?: false,
            likeCount = shareCount ?: 0,
            coverImageKey = coverUrl ?: title,
            authorName = authorName,
            authorAvatarInitials = authorInitials,
            authorAvatarColor = authorAvatarColor,
            timeAgo = formatTimeAgo(createdAt),
        )
    }

    private fun UserProfileResponseDto.toUserProfile(
        currentUserId: String?,
        isFollowing: Boolean,
        posts: List<CommunityPost>,
        publicItineraries: List<LinkedItinerary> = emptyList(),
    ): UserProfile {
        val displayName = name ?: "Người dùng"
        return UserProfile(
            id = id,
            name = displayName,
            avatarUrl = avatarUrl ?: "",
            coverUrl = coverUrl ?: "",
            bio = bio ?: "",
            username = username?.takeIf { it.isNotBlank() }
                ?: deriveUsername(displayName),
            websiteUrl = websiteUrl ?: "",
            totalProvinces = totalProvinces ?: 0,
            totalPlacesVisited = totalPlacesVisited ?: 0,
            explorerLevel = ExplorerLevel.from(explorerLevel ?: "newbie"),
            followerCount = followerCount ?: 0,
            followingCount = followingCount ?: 0,
            postCount = postCount ?: 0,
            isVerified = isVerified ?: false,
            isPrivate = isPrivate ?: false,
            avatarInitials = getInitials(displayName),
            avatarColor = getAvatarColor(displayName),
            isOwnProfile = currentUserId != null && id == currentUserId,
            isFollowing = isFollowing,
            posts = posts,
            publicItineraries = publicItineraries,
        )
    }

    private fun deriveUsername(name: String): String =
        "@" + name.lowercase().replace("\\s+".toRegex(), ".")

    private fun PostResponseBackendDto.toCommunityPost(
        currentUserId: String?,
        likedPostIds: Set<String>,
    ): CommunityPost {
        val authorNameVal = author?.name ?: "Người dùng"
        val mediaList = media?.map {
            PostMedia(
                id = it.id ?: "",
                mediaUrl = it.mediaUrl ?: "",
                mediaType = it.mediaType ?: "image",
                orderIndex = it.orderIndex ?: 0,
            )
        } ?: emptyList()

        val linkedItineraryVal = itinerary?.let {
            LinkedItinerary(
                id = it.id,
                title = it.title ?: "",
                stopCount = 0,
                isPublic = it.isPublic ?: true,
            )
        }

        return CommunityPost(
            id = id,
            userId = author?.id ?: "",
            authorName = authorNameVal,
            authorAvatarUrl = author?.avatarUrl ?: "",
            authorAvatarInitials = getInitials(authorNameVal),
            authorAvatarColor = getAvatarColor(authorNameVal),
            timeAgo = formatTimeAgo(createdAt).uppercase(),
            content = content ?: "",
            postType = if (postType == "text" || postType == "image") "original" else (postType ?: "original"),
            media = mediaList,
            likeCount = reactionCount ?: 0,
            commentCount = commentCount ?: 0,
            repostCount = repostCount ?: 0,
            isLiked = likedPostIds.contains(id),
            linkedItinerary = linkedItineraryVal,
        )
    }

    private fun getAvatarColor(name: String): Long {
        val colors = listOf(
            0xFF2563EB,
            0xFF7C3AED,
            0xFF059669,
            0xFFEA580C,
            0xFFDB2777,
            0xFF0891B2,
            0xFFC6102E,
        )
        return colors[Math.abs(name.hashCode()) % colors.size]
    }

    private fun getInitials(name: String): String {
        if (name.isBlank()) return "U"
        val parts = name.trim().split("\\s+".toRegex())
        if (parts.size == 1) return parts[0].take(2).uppercase()
        return (parts.first().take(1) + parts.last().take(1)).uppercase()
    }

    private fun formatTimeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "vừa xong"
        return try {
            val parsed = OffsetDateTime.parse(isoString)
            val now = OffsetDateTime.now(parsed.offset)
            val seconds = Duration.between(parsed, now).seconds
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
}

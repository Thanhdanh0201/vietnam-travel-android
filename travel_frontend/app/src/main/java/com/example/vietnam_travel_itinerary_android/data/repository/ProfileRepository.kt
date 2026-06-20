package com.example.vietnam_travel_itinerary_android.data.repository

import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.ItineraryResponseDto
import com.example.vietnam_travel_itinerary_android.data.dto.PostResponseBackendDto
import com.example.vietnam_travel_itinerary_android.data.dto.UpdateProfileRequest
import com.example.vietnam_travel_itinerary_android.data.dto.UserProfileResponseDto
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.data.session.UserSessionCache
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

    suspend fun loadSessionProfile(): UserProfile = withContext(Dispatchers.IO) {
        val currentUserId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not authenticated")
        val token = supabase.auth.currentAccessTokenOrNull()?.let { "Bearer $it" }
            ?: throw IllegalStateException("Not authenticated")
        val profileDto = fetchProfileDto(currentUserId, token)
        profileDto.toUserProfile(
            currentUserId = currentUserId,
            isFollowing = false,
            posts = emptyList(),
        ).also { profile ->
            UserSessionCache.set(profile)
        }
    }

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

        val savedPosts = if (isOwnProfile && token != null && currentUserId != null) {
            try {
                val postsDto = api.getSavedPosts(token, limit = 50, offset = 0)
                val likedPostIds = if (postsDto.isNotEmpty()) {
                    try {
                        api.checkLikedPosts(token, postsDto.map { it.id }).toSet()
                    } catch (e: Exception) {
                        emptySet()
                    }
                } else emptySet()
                postsDto.map {
                    communityRepository.mapPostResponse(
                        it,
                        currentUserId,
                        likedPostIds,
                        savedPostIds = postsDto.map { post -> post.id }.toSet(),
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else emptyList()

        profileDto.toUserProfile(
            currentUserId = currentUserId,
            isFollowing = isFollowing,
            posts = posts,
            savedPosts = savedPosts,
            publicItineraries = itineraries,
        ).also { profile ->
            if (profile.isOwnProfile) {
                UserSessionCache.set(profile)
            }
        }
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

    suspend fun getFollowers(userId: String): Result<List<FollowListUser>> = withContext(Dispatchers.IO) {
        try {
            val users = api.getFollowers(userId).mapNotNull { item ->
                item.follower?.toFollowListUser() ?: item.followerId.takeIf { it.isNotBlank() }?.let { id ->
                    FollowListUser(id = id, name = "Người dùng")
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getFollowing(userId: String): Result<List<FollowListUser>> = withContext(Dispatchers.IO) {
        try {
            val users = api.getFollowing(userId).mapNotNull { item ->
                item.following?.toFollowListUser() ?: item.followingId.takeIf { it.isNotBlank() }?.let { id ->
                    FollowListUser(id = id, name = "Người dùng")
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun com.example.vietnam_travel_itinerary_android.data.dto.UserCompactDto.toFollowListUser(): FollowListUser {
        val displayName = name?.takeIf { it.isNotBlank() } ?: "Người dùng"
        return FollowListUser(
            id = id,
            name = displayName,
            avatarUrl = avatarUrl ?: "",
            avatarInitials = getInitials(displayName),
            avatarColor = getAvatarColor(displayName),
            isVerified = isVerified ?: false,
            explorerLevel = ExplorerLevel.from(explorerLevel ?: "newbie"),
        )
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

    suspend fun savePost(userId: String, postId: String): Boolean =
        communityRepository.savePost(userId, postId)

    suspend fun unsavePost(userId: String, postId: String): Boolean =
        communityRepository.unsavePost(userId, postId)

    suspend fun deletePost(postId: String): Boolean =
        communityRepository.deletePost(postId)

    suspend fun reportUser(reportedUserId: String, reason: String, description: String?): Boolean {
        val reporterId = supabase.auth.currentUserOrNull()?.id ?: return false
        return communityRepository.report(
            userId = reporterId,
            reason = reason,
            reportedPostId = null,
            reportedCommentId = null,
            reportedUserId = reportedUserId,
            description = description,
        )
    }


    private suspend fun fetchProfileDto(userId: String, token: String?): UserProfileResponseDto {
        try {
            val profile = api.getProfile(userId, token)
            // Nếu user đã tồn tại nhưng chưa được verify trên backend, ta cần đồng bộ lại
            if (profile.isVerified != true && token != null) {
                val user = supabase.auth.currentUserOrNull()
                if (user != null && user.id == userId) {
                    val metaName = user.userMetadata?.get("full_name")?.toString()?.takeIf { it.isNotBlank() }
                        ?: user.userMetadata?.get("name")?.toString()?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore("@")
                        ?: "Traveler"
                    val syncRequest = UserSyncRequest(
                        id = user.id,
                        email = user.email ?: "",
                        name = metaName,
                    )
                    val syncResponse = api.syncUser(token, syncRequest)
                    if (syncResponse.isSuccessful) {
                        return api.getProfile(userId, token)
                    }
                }
            }
            return profile
        } catch (e: Exception) {
            e.printStackTrace()
            if (token == null) throw e

            val user = supabase.auth.currentUserOrNull()
            if (user != null && user.id == userId) {
                val metaName = user.userMetadata?.get("full_name")?.toString()?.takeIf { it.isNotBlank() }
                    ?: user.userMetadata?.get("name")?.toString()?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "Traveler"
                val syncRequest = UserSyncRequest(
                    id = user.id,
                    email = user.email ?: "",
                    name = metaName,
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

            val savedPostIds = if (token != null && postsDto.isNotEmpty()) {
                try {
                    api.checkSavedPosts(token, postsDto.map { it.id }).toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else emptySet()

            postsDto.map {
                communityRepository.mapPostResponse(it, currentUserId, likedPostIds, savedPostIds)
            }
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
        savedPosts: List<CommunityPost> = emptyList(),
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
            role = role ?: "user",
            avatarInitials = getInitials(displayName),
            avatarColor = getAvatarColor(displayName),
            isOwnProfile = currentUserId != null && id == currentUserId,
            isFollowing = isFollowing,
            posts = posts,
            savedPosts = savedPosts,
            publicItineraries = publicItineraries,
        )
    }

    private fun deriveUsername(name: String): String =
        "@" + name.lowercase().replace("\\s+".toRegex(), ".")

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
            val parsed = java.time.OffsetDateTime.parse(isoString).toInstant()
            val now = java.time.Instant.now()
            val seconds = java.time.Duration.between(parsed, now).seconds.coerceAtLeast(0)
            when {
                seconds < 60 -> "vừa xong"
                seconds < 3600 -> "${seconds / 60} phút trước"
                seconds < 86400 -> "${seconds / 3600} giờ trước"
                seconds < 604800 -> "${seconds / 86400} ngày trước"
                else -> {
                    val localDateTime = java.time.LocalDateTime.ofInstant(parsed, java.time.ZoneId.systemDefault())
                    localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }
            }
        } catch (e: Exception) {
            "vừa xong"
        }
    }
}

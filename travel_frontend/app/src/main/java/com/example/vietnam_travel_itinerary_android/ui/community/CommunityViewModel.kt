package com.example.vietnam_travel_itinerary_android.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.*
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.data.repository.CommunityRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class CommunityViewModel(
    private val repository: CommunityRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isFollowingFilter = MutableStateFlow(false)
    val isFollowingFilter: StateFlow<Boolean> = _isFollowingFilter.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    val currentUserId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: "00000000-0000-0000-0000-000000000000"

    private var feedChannel: RealtimeChannel? = null
    private var activePostChannel: RealtimeChannel? = null
    private var notificationChannel: RealtimeChannel? = null

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        loadUserProfile()
                        loadFeed()
                        subscribeToRealtimeFeed()
                        subscribeToRealtimeNotifications()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        loadUserProfile()
                        loadFeed()
                        subscribeToRealtimeFeed()
                        unsubscribeFromRealtimeNotifications()
                    }
                    else -> {
                        // Initializing... wait until state is resolved
                    }
                }
            }
        }
    }

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

    private fun getDisplayNameFromSupabase(): String {
        val user = supabase.auth.currentUserOrNull() ?: return "Traveler"
        val metadata = user.userMetadata
        if (metadata != null) {
            val nameJson = metadata["name"] ?: metadata["full_name"] ?: metadata["username"]
            if (nameJson != null) {
                val name = nameJson.jsonPrimitive.content
                if (name.isNotBlank()) return name
            }
        }
        val email = user.email
        if (!email.isNullOrBlank()) {
            return email.substringBefore("@")
        }
        return "Traveler"
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                val token = supabase.auth.currentAccessTokenOrNull()
                if (userId != null && token != null) {
                    var profileDto: UserProfileResponseDto? = null
                    try {
                        profileDto = RetrofitInstance.api.getProfile(userId, "Bearer $token")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // User not found in Spring Boot backend, try to sync user first!
                        val user = supabase.auth.currentUserOrNull()
                        if (user != null) {
                            val syncRequest = UserSyncRequest(
                                id = user.id,
                                email = user.email ?: "",
                                name = getDisplayNameFromSupabase()
                            )
                            val syncResponse = RetrofitInstance.api.syncUser("Bearer $token", syncRequest)
                            if (syncResponse.isSuccessful) {
                                // Try fetching profile again after sync
                                profileDto = RetrofitInstance.api.getProfile(userId, "Bearer $token")
                            }
                        }
                    }

                    if (profileDto != null) {
                        _currentUserProfile.value = UserProfile(
                            id = profileDto.id.toString(),
                            name = profileDto.name ?: "",
                            avatarUrl = profileDto.avatarUrl ?: "",
                            avatarInitials = getInitials(profileDto.name ?: ""),
                            avatarColor = getAvatarColor(profileDto.name ?: ""),
                            explorerLevel = ExplorerLevel.from(profileDto.explorerLevel ?: "newbie"),
                            isVerified = profileDto.isVerified ?: false
                        )
                    } else {
                        // Fallback default user profile for testing
                        _currentUserProfile.value = UserProfile(
                            id = currentUserId,
                            name = "Bạn",
                            avatarInitials = "BN",
                            avatarColor = 0xFFC6102E
                        )
                    }
                } else {
                    // Fallback default user profile for testing
                    _currentUserProfile.value = UserProfile(
                        id = currentUserId,
                        name = "Bạn",
                        avatarInitials = "BN",
                        avatarColor = 0xFFC6102E
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Default fallback
                _currentUserProfile.value = UserProfile(
                    id = currentUserId,
                    name = "Bạn",
                    avatarInitials = "BN",
                    avatarColor = 0xFFC6102E
                )
            }
        }
    }

    fun loadFeed() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val list = if (_isFollowingFilter.value) {
                    repository.getFollowingFeed(currentUserId)
                } else {
                    repository.getPublicFeed(currentUserId)
                }
                _posts.value = list
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Không thể tải bảng tin: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFollowFilter(followingOnly: Boolean) {
        if (_isFollowingFilter.value != followingOnly) {
            _isFollowingFilter.value = followingOnly
            loadFeed()
        }
    }

    fun createPost(content: String, mediaUrls: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                val newPost = repository.createPost(
                    userId = currentUserId,
                    content = content,
                    postType = if (mediaUrls.isNotEmpty()) "image" else "text",
                    mediaUrls = mediaUrls
                )
                // Prepend locally (if realtime is laggy, we see it instantly)
                _posts.update { current ->
                    if (current.any { it.id == newPost.id }) current
                    else listOf(newPost) + current
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Đăng bài thất bại: ${e.message}"
            }
        }
    }

    fun likePost(postId: String) {
        // Optimistic UI update
        _posts.update { list ->
            list.map {
                if (it.id == postId) {
                    it.copy(isLiked = true, likeCount = it.likeCount + 1)
                } else it
            }
        }

        viewModelScope.launch {
            val success = repository.likePost(currentUserId, postId)
            if (!success) {
                // Rollback if failed
                _posts.update { list ->
                    list.map {
                        if (it.id == postId) {
                            it.copy(isLiked = false, likeCount = (it.likeCount - 1).coerceAtLeast(0))
                        } else it
                    }
                }
            }
        }
    }

    fun unlikePost(postId: String) {
        // Optimistic UI update
        _posts.update { list ->
            list.map {
                if (it.id == postId) {
                    it.copy(isLiked = false, likeCount = (it.likeCount - 1).coerceAtLeast(0))
                } else it
            }
        }

        viewModelScope.launch {
            val success = repository.unlikePost(currentUserId, postId)
            if (!success) {
                // Rollback if failed
                _posts.update { list ->
                    list.map {
                        if (it.id == postId) {
                            it.copy(isLiked = true, likeCount = it.likeCount + 1)
                        } else it
                    }
                }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            val success = repository.deletePost(postId)
            if (success) {
                _posts.update { current ->
                    current.filter { it.id != postId }
                }
            } else {
                _error.value = "Xoá bài đăng thất bại."
            }
        }
    }

    fun repostPost(postId: String, quoteText: String? = null) {
        viewModelScope.launch {
            val success = repository.repostPost(currentUserId, postId, quoteText)
            if (success) {
                // Update local repost counter
                _posts.update { list ->
                    list.map {
                        if (it.id == postId) {
                            it.copy(repostCount = it.repostCount + 1)
                        } else it
                    }
                }
                loadFeed()
            } else {
                _error.value = "Chia sẻ bài viết thất bại."
            }
        }
    }

    fun undoRepost(postId: String) {
        viewModelScope.launch {
            val success = repository.undoRepost(currentUserId, postId)
            if (success) {
                _posts.update { list ->
                    list.map {
                        if (it.id == postId) {
                            it.copy(repostCount = (it.repostCount - 1).coerceAtLeast(0))
                        } else it
                    }
                }
                loadFeed()
            }
        }
    }

    // --- Comment Screen Operations ---
    fun loadComments(postId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val list = repository.getComments(postId, currentUserId)
                _comments.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun postComment(postId: String, content: String, parentCommentId: String? = null) {
        viewModelScope.launch {
            try {
                val newComment = repository.postComment(postId, currentUserId, content, parentCommentId)
                
                // Locally append to update state immediately
                if (parentCommentId != null) {
                    _comments.update { list ->
                        list.map { comment ->
                            if (comment.id == parentCommentId) {
                                comment.copy(
                                    replies = comment.replies + newComment,
                                    replyCount = comment.replyCount + 1
                                )
                            } else comment
                        }
                    }
                } else {
                    _comments.update { listOf(newComment) + it }
                }

                // Increment comment count in posts list
                _posts.update { list ->
                    list.map {
                        if (it.id == postId) {
                            it.copy(commentCount = it.commentCount + 1)
                        } else it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun likeComment(commentId: String) {
        _comments.update { list ->
            list.map {
                if (it.id == commentId) it.copy(isLiked = true, reactionCount = it.reactionCount + 1)
                else {
                    val updatedReplies = it.replies.map { r ->
                        if (r.id == commentId) r.copy(isLiked = true, reactionCount = r.reactionCount + 1)
                        else r
                    }
                    it.copy(replies = updatedReplies)
                }
            }
        }

        viewModelScope.launch {
            repository.likeComment(commentId, currentUserId)
        }
    }

    fun unlikeComment(commentId: String) {
        _comments.update { list ->
            list.map {
                if (it.id == commentId) it.copy(isLiked = false, reactionCount = (it.reactionCount - 1).coerceAtLeast(0))
                else {
                    val updatedReplies = it.replies.map { r ->
                        if (r.id == commentId) r.copy(isLiked = false, reactionCount = (r.reactionCount - 1).coerceAtLeast(0))
                        else r
                    }
                    it.copy(replies = updatedReplies)
                }
            }
        }

        viewModelScope.launch {
            repository.unlikeComment(commentId, currentUserId)
        }
    }

    fun reportPostOrComment(reason: String, reportedPostId: String?, reportedCommentId: String?, description: String?) {
        viewModelScope.launch {
            repository.report(currentUserId, reason, reportedPostId, reportedCommentId, description)
        }
    }

    // --- Realtime Subscriptions ---
    private fun subscribeToRealtimeFeed() {
        unsubscribeFromRealtimeFeed()
        viewModelScope.launch {
            try {
                feedChannel = supabase.channel("feed-channel")

                // Listen for Insert events in posts table
                val postFlow = feedChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "posts"
                }
                postFlow.onEach { action ->
                    val newPostId = action.record["id"]?.jsonPrimitive?.content ?: return@onEach
                    val detailedPost = repository.getPostDetails(newPostId, currentUserId)
                    if (detailedPost != null) {
                        _posts.update { currentList ->
                            if (currentList.any { it.id == detailedPost.id }) currentList
                            else listOf(detailedPost) + currentList
                        }
                    }
                }.launchIn(viewModelScope)

                // Listen for Delete events in posts table
                val deleteFlow = feedChannel!!.postgresChangeFlow<PostgresAction.Delete>(schema = "public") {
                    table = "posts"
                }
                deleteFlow.onEach { action ->
                    val deletedPostId = action.oldRecord["id"]?.jsonPrimitive?.content ?: return@onEach
                    _posts.update { currentList ->
                        currentList.filter { it.id != deletedPostId }
                    }
                }.launchIn(viewModelScope)

                feedChannel!!.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun subscribeToPostDetails(postId: String) {
        unsubscribeFromPostDetails()

        viewModelScope.launch {
            try {
                activePostChannel = supabase.channel("post_detail_$postId")

                // Comments updates
                val commentFlow = activePostChannel!!.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "comments"
                    filter("post_id", FilterOperator.EQ, postId)
                }
                commentFlow.onEach { action ->
                    // Reload all comments to ensure we get joined details
                    val freshComments = repository.getComments(postId, currentUserId)
                    _comments.value = freshComments

                    // Sync the post comment count in the list
                    val count = freshComments.size + freshComments.sumOf { it.replies.size }
                    _posts.update { list ->
                        list.map { if (it.id == postId) it.copy(commentCount = count) else it }
                    }
                }.launchIn(viewModelScope)

                // Likes count updates
                val reactionFlow = activePostChannel!!.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "post_reactions"
                    filter("post_id", FilterOperator.EQ, postId)
                }
                reactionFlow.onEach { action ->
                    val updatedPost = repository.getPostDetails(postId, currentUserId)
                    if (updatedPost != null) {
                        _posts.update { list ->
                            list.map { if (it.id == postId) updatedPost else it }
                        }
                    }
                }.launchIn(viewModelScope)

                activePostChannel!!.subscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unsubscribeFromPostDetails() {
        activePostChannel?.let {
            viewModelScope.launch {
                try {
                    it.unsubscribe()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        activePostChannel = null
    }

    private fun subscribeToRealtimeNotifications() {
        unsubscribeFromRealtimeNotifications()
        viewModelScope.launch {
            try {
                notificationChannel = supabase.channel("notification-channel")
                
                val notifFlow = notificationChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "notifications"
                    filter("user_id", FilterOperator.EQ, currentUserId)
                }
                
                notifFlow.onEach {
                    _unreadNotificationsCount.update { it + 1 }
                }.launchIn(viewModelScope)

                notificationChannel!!.subscribe()
                
                // Fetch initial count of unread notifications
                val notifs = repository.getNotifications(currentUserId)
                val initialUnread = notifs.count { !it.is_read }
                _unreadNotificationsCount.value = initialUnread
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun unsubscribeFromRealtimeFeed() {
        feedChannel?.let {
            viewModelScope.launch {
                try {
                    it.unsubscribe()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        feedChannel = null
    }

    fun unsubscribeFromRealtimeNotifications() {
        notificationChannel?.let {
            viewModelScope.launch {
                try {
                    it.unsubscribe()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        notificationChannel = null
    }

    override fun onCleared() {
        super.onCleared()
        // Unsubscribe all channels to prevent leaks
        viewModelScope.launch {
            try {
                feedChannel?.unsubscribe()
                activePostChannel?.unsubscribe()
                notificationChannel?.unsubscribe()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

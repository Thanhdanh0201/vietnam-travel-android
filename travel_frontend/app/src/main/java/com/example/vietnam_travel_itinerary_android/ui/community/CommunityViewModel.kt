package com.example.vietnam_travel_itinerary_android.ui.community

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.*
import com.example.vietnam_travel_itinerary_android.data.model.*
import com.example.vietnam_travel_itinerary_android.data.repository.CommunityRepository
import com.example.vietnam_travel_itinerary_android.data.session.UserSessionCache
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val pageSize = 20
    private var currentOffset = 0

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isFollowingFilter = MutableStateFlow(false)
    val isFollowingFilter: StateFlow<Boolean> = _isFollowingFilter.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    // ── Image & Place state cho CreatePostWidget
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()

    private val _shareItineraryId = MutableStateFlow<String?>(null)
    val shareItineraryId: StateFlow<String?> = _shareItineraryId.asStateFlow()

    fun setShareItineraryId(id: String?) {
        _shareItineraryId.value = id
    }

    private val _openedPostId = MutableStateFlow<String?>(null)
    val openedPostId: StateFlow<String?> = _openedPostId.asStateFlow()

    private val _highlightCommentId = MutableStateFlow<String?>(null)
    val highlightCommentId: StateFlow<String?> = _highlightCommentId.asStateFlow()

    private val _focusCommentInput = MutableStateFlow(false)
    val focusCommentInput: StateFlow<Boolean> = _focusCommentInput.asStateFlow()

    fun setOpenedPostId(postId: String?) {
        _openedPostId.value = postId
        if (postId == null) {
            _highlightCommentId.value = null
            _focusCommentInput.value = false
        }
    }

    fun setHighlightCommentId(commentId: String?) {
        _highlightCommentId.value = commentId
    }

    fun setFocusCommentInput(focus: Boolean) {
        _focusCommentInput.value = focus
    }

    private val _selectedPlace = MutableStateFlow<PostPlace?>(null)
    val selectedPlace: StateFlow<PostPlace?> = _selectedPlace.asStateFlow()

    private val _searchPlaceResults = MutableStateFlow<List<Place>>(emptyList())
    val searchPlaceResults: StateFlow<List<Place>> = _searchPlaceResults.asStateFlow()

    private val _searchProvinceResults = MutableStateFlow<List<Province>>(emptyList())
    val searchProvinceResults: StateFlow<List<Province>> = _searchProvinceResults.asStateFlow()

    private val _provincePlacesResults = MutableStateFlow<List<Place>>(emptyList())
    val provincePlacesResults: StateFlow<List<Place>> = _provincePlacesResults.asStateFlow()

    private val _selectedProvinceFilter = MutableStateFlow<Province?>(null)
    val selectedProvinceFilter: StateFlow<Province?> = _selectedProvinceFilter.asStateFlow()

    private val _isSearchingPlaces = MutableStateFlow(false)
    val isSearchingPlaces: StateFlow<Boolean> = _isSearchingPlaces.asStateFlow()

    private val _isCreatingPost = MutableStateFlow(false)
    val isCreatingPost: StateFlow<Boolean> = _isCreatingPost.asStateFlow()

    private var searchJob: Job? = null

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
                        try {
                            supabase.realtime.connect()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        subscribeToRealtimeFeed()
                        subscribeToRealtimeNotifications()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        loadUserProfile()
                        loadFeed()
                        try {
                            supabase.realtime.connect()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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
                val cached = UserSessionCache.get()
                if (cached != null && _currentUserProfile.value == null) {
                    _currentUserProfile.value = cached
                }

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
                        val profile = UserProfile(
                            id = profileDto.id.toString(),
                            name = profileDto.name ?: "",
                            avatarUrl = profileDto.avatarUrl ?: "",
                            avatarInitials = getInitials(profileDto.name ?: ""),
                            avatarColor = getAvatarColor(profileDto.name ?: ""),
                            explorerLevel = ExplorerLevel.from(profileDto.explorerLevel ?: "newbie"),
                            isVerified = profileDto.isVerified ?: false
                        )
                        _currentUserProfile.value = profile
                        UserSessionCache.set(profile)
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
        if (_isLoading.value) return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                reloadFeedFromStart()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Không thể tải bảng tin: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        if (_isRefreshing.value || _isLoading.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                reloadFeedFromStart()
                loadUserProfile()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Không thể làm mới bảng tin: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun reloadFeedFromStart() {
        currentOffset = 0
        _hasMore.value = true
        val list = fetchFeedPage(offset = 0)
        currentOffset = list.size
        _hasMore.value = list.size >= pageSize
        _posts.value = list
    }

    fun loadMore() {
        if (_isLoadingMore.value || _isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val list = fetchFeedPage(offset = currentOffset)
                currentOffset += list.size
                _hasMore.value = list.size >= pageSize
                _posts.update { current ->
                    val existingIds = current.map { it.id }.toSet()
                    current + list.filter { it.id !in existingIds }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Không thể tải thêm bài viết: ${e.message}"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private suspend fun fetchFeedPage(offset: Int): List<CommunityPost> {
        return if (_isFollowingFilter.value) {
            repository.getFollowingFeed(currentUserId, limit = pageSize, offset = offset)
        } else {
            repository.getPublicFeed(currentUserId, limit = pageSize, offset = offset)
        }
    }

    fun toggleFollowFilter(followingOnly: Boolean) {
        if (_isFollowingFilter.value != followingOnly) {
            _isFollowingFilter.value = followingOnly
            loadFeed()
        }
    }

    fun fetchPostDetails(postId: String, onSuccess: (CommunityPost) -> Unit) {
        viewModelScope.launch {
            try {
                val post = repository.getPostDetails(postId, currentUserId)
                if (post != null) {
                    onSuccess(post)
                }
            } catch (e: Exception) {
                _error.value = "Không thể tải chi tiết bài viết: ${e.localizedMessage}"
            }
        }
    }

    // ── Image management
    fun addImages(uris: List<Uri>) {
        _selectedImages.update { current ->
            (current + uris).take(4) // Max 4 images
        }
    }

    fun removeImage(index: Int) {
        _selectedImages.update { current ->
            current.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
        }
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
    }

    // ── Place management
    fun selectPlace(place: Place) {
        _selectedPlace.value = PostPlace(
            id = place.id,
            name = place.name,
            lat = place.lat,
            lng = place.lng,
            imageUrl = place.imageUrl ?: "",
            provinceName = place.provinces?.name ?: ""
        )
    }

    fun clearPlace() {
        _selectedPlace.value = null
    }

    // ── Place search with debounce
    fun searchPlaces(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _searchPlaceResults.value = emptyList()
            _searchProvinceResults.value = emptyList()
            _provincePlacesResults.value = emptyList()
            _selectedProvinceFilter.value = null
            _isSearchingPlaces.value = false
            return
        }
        _isSearchingPlaces.value = true
        searchJob = viewModelScope.launch {
            delay(300) // Debounce 300ms
            try {
                val placesDeferred = async { repository.searchPlaces(query) }
                val provincesDeferred = async { repository.searchProvinces(query) }
                _searchPlaceResults.value = placesDeferred.await()
                _searchProvinceResults.value = provincesDeferred.await()
                _provincePlacesResults.value = emptyList()
                _selectedProvinceFilter.value = null
            } catch (e: Exception) {
                e.printStackTrace()
                _searchPlaceResults.value = emptyList()
                _searchProvinceResults.value = emptyList()
            } finally {
                _isSearchingPlaces.value = false
            }
        }
    }

    fun loadPlacesForProvince(province: Province) {
        _selectedProvinceFilter.value = province
        _isSearchingPlaces.value = true
        viewModelScope.launch {
            try {
                _provincePlacesResults.value = repository.getPlacesByProvince(province.code, limit = 30)
            } catch (e: Exception) {
                e.printStackTrace()
                _provincePlacesResults.value = emptyList()
            } finally {
                _isSearchingPlaces.value = false
            }
        }
    }

    fun clearProvinceFilter() {
        _selectedProvinceFilter.value = null
        _provincePlacesResults.value = emptyList()
    }

    fun clearSearchResults() {
        _searchPlaceResults.value = emptyList()
        _searchProvinceResults.value = emptyList()
        _provincePlacesResults.value = emptyList()
        _selectedProvinceFilter.value = null
    }

    // ── Create post with image upload
    fun createPost(
        content: String,
        mediaUris: List<Uri> = emptyList(),
        itineraryId: String? = null,
        placeId: String? = null,
        contentResolver: android.content.ContentResolver? = null
    ) {
        _isCreatingPost.value = true
        // Clear selection state immediately for responsive UI
        // (mediaUris is already a snapshot passed by caller)
        _selectedImages.value = emptyList()
        _selectedPlace.value = null

        viewModelScope.launch {
            try {
                // Upload images to Supabase Storage if any
                val mediaUrls = mutableListOf<String>()
                if (contentResolver != null && mediaUris.isNotEmpty()) {
                    for (uri in mediaUris) {
                        try {
                            val inputStream = contentResolver.openInputStream(uri)
                            val bytes = inputStream?.readBytes() ?: continue
                            inputStream.close()
                            val fileName = "img_${System.currentTimeMillis()}_${mediaUrls.size}.jpg"
                            val publicUrl = repository.uploadMedia(bytes, fileName)
                            mediaUrls.add(publicUrl)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Skip failed uploads
                        }
                    }
                }

                val postType = when {
                    itineraryId != null -> "itinerary_share"
                    else -> "text"
                }

                val newPost = repository.createPost(
                    userId = currentUserId,
                    content = content,
                    postType = postType,
                    itineraryId = itineraryId,
                    placeId = placeId,
                    mediaUrls = mediaUrls
                )

                // Prepend locally (if realtime is laggy, we see it instantly)
                _posts.update { current ->
                    if (current.any { it.id == newPost.id }) current
                    else listOf(newPost) + current
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                e.printStackTrace()
                println("CREATE POST ERROR: HTTP ${e.code()} — $errorBody")
                _error.value = "Đăng bài thất bại (${e.code()}): ${errorBody?.take(200) ?: e.message()}"
            } catch (e: Exception) {
                e.printStackTrace()
                println("CREATE POST ERROR: ${e.javaClass.simpleName} — ${e.message}")
                _error.value = "Đăng bài thất bại: ${e.message}"
            } finally {
                _isCreatingPost.value = false
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

    fun savePost(postId: String) {
        _posts.update { list ->
            list.map {
                if (it.id == postId) it.copy(isSaved = true) else it
            }
        }
        viewModelScope.launch {
            val success = repository.savePost(currentUserId, postId)
            if (!success) {
                _posts.update { list ->
                    list.map {
                        if (it.id == postId) it.copy(isSaved = false) else it
                    }
                }
            }
        }
    }

    fun unsavePost(postId: String) {
        _posts.update { list ->
            list.map {
                if (it.id == postId) it.copy(isSaved = false) else it
            }
        }
        viewModelScope.launch {
            val success = repository.unsavePost(currentUserId, postId)
            if (!success) {
                _posts.update { list ->
                    list.map {
                        if (it.id == postId) it.copy(isSaved = true) else it
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
                _error.value = null
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

    fun postComment(
        postId: String,
        content: String,
        parentCommentId: String? = null,
        imageUri: Uri? = null,
        contentResolver: android.content.ContentResolver? = null,
    ) {
        viewModelScope.launch {
            try {
                var uploadedImageUrl: String? = null
                if (imageUri != null && contentResolver != null) {
                    try {
                        val inputStream = contentResolver.openInputStream(imageUri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()
                        if (bytes != null) {
                            uploadedImageUrl = repository.uploadMedia(
                                bytes,
                                "comment_${System.currentTimeMillis()}.jpg"
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val trimmed = content.trim()
                if (trimmed.isBlank() && uploadedImageUrl.isNullOrBlank()) return@launch

                val finalContent = trimmed.ifBlank { "📷" }
                val newComment = repository.postComment(
                    postId = postId,
                    userId = currentUserId,
                    content = finalContent,
                    parentCommentId = parentCommentId,
                    imageUrl = uploadedImageUrl,
                )
                
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
            } catch (e: retrofit2.HttpException) {
                e.printStackTrace()
                val errorBody = e.response()?.errorBody()?.string()
                _error.value = "Không thể gửi bình luận (${e.code()}): ${errorBody?.take(500) ?: e.message()}"
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Lỗi kết nối bình luận: ${e.localizedMessage}"
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

    fun reportPostOrComment(
        reason: String,
        reportedPostId: String?,
        reportedCommentId: String?,
        description: String?,
        reportedUserId: String? = null,
    ) {
        viewModelScope.launch {
            repository.report(currentUserId, reason, reportedPostId, reportedCommentId, reportedUserId, description)
        }
    }

    private fun removePostFromFeed(postId: String) {
        _posts.update { it.filter { post -> post.id != postId } }
        if (_openedPostId.value == postId) {
            _openedPostId.value = null
        }
    }

    private fun isSoftDeletedPostUpdate(action: PostgresAction.Update): Boolean {
        val flag = action.record["is_deleted"]?.jsonPrimitive?.content
        return flag == "true" || flag == "t"
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
                    try {
                        val detailedPost = repository.getPostDetails(newPostId, currentUserId)
                        if (detailedPost != null) {
                            _posts.update { currentList ->
                                if (currentList.any { it.id == detailedPost.id }) currentList
                                else listOf(detailedPost) + currentList
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.launchIn(viewModelScope)

                // Listen for Update events in posts table
                val updateFlow = feedChannel!!.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                    table = "posts"
                }
                updateFlow.onEach { action ->
                    val updatedPostId = action.record["id"]?.jsonPrimitive?.content ?: return@onEach
                    if (isSoftDeletedPostUpdate(action)) {
                        removePostFromFeed(updatedPostId)
                        return@onEach
                    }
                    try {
                        val detailedPost = repository.getPostDetails(updatedPostId, currentUserId)
                        if (detailedPost != null) {
                            _posts.update { currentList ->
                                currentList.map { post ->
                                    if (post.id == detailedPost.id) detailedPost else post
                                }
                            }
                        } else {
                            removePostFromFeed(updatedPostId)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        removePostFromFeed(updatedPostId)
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
                reactionFlow.onEach {
                    try {
                        val updatedPost = repository.getPostDetails(postId, currentUserId)
                        if (updatedPost != null) {
                            _posts.update { list ->
                                list.map { if (it.id == postId) updatedPost else it }
                            }
                        } else {
                            removePostFromFeed(postId)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
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

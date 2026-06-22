package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.UserProfile
import com.example.vietnam_travel_itinerary_android.data.repository.ProfileRepository
import com.example.vietnam_travel_itinerary_android.data.session.UserSessionCache
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    data class ProfileUiState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val profile: UserProfile? = null,
        val error: String? = null,
        val isFollowLoading: Boolean = false,
    )

    private var lastLoadedUserId: String? = null
    private var lastLoadedAtMs: Long = 0L

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            isLoading = UserSessionCache.get() == null,
            profile = UserSessionCache.get(),
        ),
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    fun loadProfile(userId: String? = null, force: Boolean = false) {
        val targetUserId = userId ?: currentUserId
        if (targetUserId.isNullOrBlank()) {
            _uiState.value = ProfileUiState(
                isLoading = false,
                error = "Vui lòng đăng nhập để xem hồ sơ.",
            )
            return
        }

        val existingProfile = _uiState.value.profile
        val hasExistingProfile = existingProfile?.id == targetUserId
        val cachedProfile = UserSessionCache.get()?.takeIf {
            it.id == targetUserId && userId == null
        }

        if (!force && hasExistingProfile && !shouldReload(targetUserId)) {
            return
        }

        if (!force && (hasExistingProfile || cachedProfile != null)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = existingProfile ?: cachedProfile,
                    error = null,
                )
            }
            fetchProfileInBackground(targetUserId, showRefreshingIndicator = hasExistingProfile)
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            fetchAndApplyProfile(targetUserId, showRefreshingIndicator = false)
        }
    }

    private fun shouldReload(userId: String): Boolean {
        if (lastLoadedUserId != userId) return true
        return System.currentTimeMillis() - lastLoadedAtMs > PROFILE_RELOAD_INTERVAL_MS
    }

    private fun fetchProfileInBackground(
        targetUserId: String,
        showRefreshingIndicator: Boolean,
    ) {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            if (showRefreshingIndicator) {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
            }
            fetchAndApplyProfile(targetUserId, showRefreshingIndicator = showRefreshingIndicator)
        }
    }

    private suspend fun fetchAndApplyProfile(
        targetUserId: String,
        showRefreshingIndicator: Boolean,
    ) {
        try {
            val profile = repository.getProfile(targetUserId, currentUserId)
            lastLoadedUserId = targetUserId
            lastLoadedAtMs = System.currentTimeMillis()
            _uiState.value = ProfileUiState(isLoading = false, profile = profile)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { state ->
                if (state.profile != null) {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Không thể làm mới hồ sơ: ${e.message}",
                    )
                } else {
                    ProfileUiState(
                        isLoading = false,
                        error = "Không thể tải hồ sơ: ${e.message}",
                    )
                }
            }
        } finally {
            if (showRefreshingIndicator) {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun refresh(userId: String? = null) {
        val targetUserId = userId ?: currentUserId
        if (targetUserId.isNullOrBlank() || _uiState.value.isRefreshing || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            fetchAndApplyProfile(targetUserId, showRefreshingIndicator = true)
        }
    }

    fun toggleFollow() {
        val profile = _uiState.value.profile ?: return
        if (profile.isOwnProfile) return

        val wasFollowing = profile.isFollowing
        val updatedFollowerCount = if (wasFollowing) {
            (profile.followerCount - 1).coerceAtLeast(0)
        } else {
            profile.followerCount + 1
        }

        _uiState.update {
            it.copy(
                profile = profile.copy(
                    isFollowing = !wasFollowing,
                    followerCount = updatedFollowerCount,
                ),
                isFollowLoading = true,
            )
        }

        viewModelScope.launch {
            val success = if (wasFollowing) {
                repository.unfollowUser(profile.id)
            } else {
                repository.followUser(profile.id)
            }

            if (!success) {
                _uiState.update {
                    it.copy(
                        profile = profile,
                        isFollowLoading = false,
                        error = "Không thể cập nhật trạng thái theo dõi.",
                    )
                }
            } else {
                _uiState.update { it.copy(isFollowLoading = false) }
            }
        }
    }

    fun likePost(postId: String) {
        val profile = _uiState.value.profile ?: return
        val userId = currentUserId ?: return

        _uiState.update { state ->
            state.copy(
                profile = profile.copy(
                    posts = profile.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(isLiked = true, likeCount = post.likeCount + 1)
                        } else post
                    },
                ),
            )
        }

        viewModelScope.launch {
            val success = repository.likePost(userId, postId)
            if (!success) rollbackLike(postId, liked = false)
        }
    }

    fun unlikePost(postId: String) {
        val profile = _uiState.value.profile ?: return
        val userId = currentUserId ?: return

        _uiState.update { state ->
            state.copy(
                profile = profile.copy(
                    posts = profile.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(
                                isLiked = false,
                                likeCount = (post.likeCount - 1).coerceAtLeast(0),
                            )
                        } else post
                    },
                ),
            )
        }

        viewModelScope.launch {
            val success = repository.unlikePost(userId, postId)
            if (!success) rollbackLike(postId, liked = true)
        }
    }

    private fun rollbackLike(postId: String, liked: Boolean) {
        _uiState.update { state ->
            val profile = state.profile ?: return@update state
            state.copy(
                profile = profile.copy(
                    posts = profile.posts.map { post ->
                        if (post.id == postId) {
                            val delta = if (liked) 1 else -1
                            post.copy(
                                isLiked = liked,
                                likeCount = (post.likeCount + delta).coerceAtLeast(0),
                            )
                        } else post
                    },
                ),
            )
        }
    }

    fun savePost(postId: String) {
        val profile = _uiState.value.profile ?: return
        val userId = currentUserId ?: return

        _uiState.update { state ->
            state.copy(
                profile = profile.copy(
                    posts = profile.posts.map { post ->
                        if (post.id == postId) post.copy(isSaved = true) else post
                    },
                    savedPosts = if (profile.savedPosts.any { it.id == postId }) {
                        profile.savedPosts.map { post ->
                            if (post.id == postId) post.copy(isSaved = true) else post
                        }
                    } else {
                        val foundPost = profile.posts.find { it.id == postId }?.copy(isSaved = true)
                        if (foundPost != null) profile.savedPosts + foundPost else profile.savedPosts
                    }
                )
            )
        }

        viewModelScope.launch {
            val success = repository.savePost(userId, postId)
            if (!success) {
                _uiState.update { state ->
                    val freshProfile = state.profile ?: return@update state
                    state.copy(
                        profile = freshProfile.copy(
                            posts = freshProfile.posts.map { post ->
                                if (post.id == postId) post.copy(isSaved = false) else post
                            },
                            savedPosts = freshProfile.savedPosts.filter { it.id != postId }
                        )
                    )
                }
            }
        }
    }

    fun unsavePost(postId: String) {
        val profile = _uiState.value.profile ?: return
        val userId = currentUserId ?: return

        _uiState.update { state ->
            state.copy(
                profile = profile.copy(
                    posts = profile.posts.map { post ->
                        if (post.id == postId) post.copy(isSaved = false) else post
                    },
                    savedPosts = profile.savedPosts.filter { it.id != postId }
                )
            )
        }

        viewModelScope.launch {
            val success = repository.unsavePost(userId, postId)
            if (!success) {
                loadProfile(profile.id)
            }
        }
    }

    fun deletePost(postId: String) {
        val profile = _uiState.value.profile ?: return
        _uiState.update { state ->
            state.copy(
                profile = profile.copy(
                    posts = profile.posts.filter { it.id != postId },
                    savedPosts = profile.savedPosts.filter { it.id != postId }
                )
            )
        }

        viewModelScope.launch {
            val success = repository.deletePost(postId)
            if (!success) {
                loadProfile(profile.id)
            }
        }
    }

    fun repostPost(postId: String, quoteText: String? = null) {
        val profile = _uiState.value.profile ?: return
        val userId = currentUserId ?: return

        viewModelScope.launch {
            val success = repository.repostPost(userId, postId, quoteText)
            if (success) {
                _uiState.update { state ->
                    val freshProfile = state.profile ?: return@update state
                    state.copy(
                        profile = freshProfile.copy(
                            posts = freshProfile.posts.map { post ->
                                if (post.id == postId) {
                                    post.copy(repostCount = post.repostCount + 1)
                                } else post
                            },
                            savedPosts = freshProfile.savedPosts.map { post ->
                                if (post.id == postId) {
                                    post.copy(repostCount = post.repostCount + 1)
                                } else post
                            }
                        )
                    )
                }
                loadProfile(profile.id, force = true)
            } else {
                _uiState.update { it.copy(error = "Chia sẻ bài viết thất bại.") }
            }
        }
    }

    fun reportUser(reason: String, description: String?) {
        val targetId = _uiState.value.profile?.id ?: return
        viewModelScope.launch {
            repository.reportUser(targetId, reason, description)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val PROFILE_RELOAD_INTERVAL_MS = 30_000L
    }
}

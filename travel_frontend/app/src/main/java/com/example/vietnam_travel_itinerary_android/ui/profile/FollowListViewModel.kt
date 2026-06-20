package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.model.FollowListType
import com.example.vietnam_travel_itinerary_android.data.model.FollowListUser
import com.example.vietnam_travel_itinerary_android.data.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FollowListUiState(
    val isLoading: Boolean = true,
    val users: List<FollowListUser> = emptyList(),
    val error: String? = null,
)

class FollowListViewModel(
    private val repository: ProfileRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowListUiState())
    val uiState = _uiState.asStateFlow()

    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    fun load(userId: String, listType: FollowListType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = when (listType) {
                FollowListType.FOLLOWERS -> repository.getFollowers(userId)
                FollowListType.FOLLOWING -> repository.getFollowing(userId)
            }
            result
                .onSuccess { users ->
                    _uiState.update { it.copy(isLoading = false, users = users, error = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Không thể tải danh sách",
                        )
                    }
                }
        }
    }
}

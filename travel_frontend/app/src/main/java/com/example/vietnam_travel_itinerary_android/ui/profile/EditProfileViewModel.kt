package com.example.vietnam_travel_itinerary_android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: ProfileRepository,
    private val supabase: SupabaseClient,
) : ViewModel() {

    data class EditProfileUiState(
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val isUploadingAvatar: Boolean = false,
        val name: String = "",
        val username: String = "",
        val bio: String = "",
        val avatarUrl: String = "",
        val avatarInitials: String = "",
        val avatarColor: Long = 0xFF64748B,
        val error: String? = null,
        val saveSuccess: Boolean = false,
    )

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun loadCurrentProfile() {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId.isNullOrBlank()) {
            _uiState.value = EditProfileUiState(
                isLoading = false,
                error = "Vui lòng đăng nhập để chỉnh sửa hồ sơ.",
            )
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val profile = repository.getProfile(userId, userId)
                _uiState.value = EditProfileUiState(
                    isLoading = false,
                    name = profile.name,
                    username = profile.username.removePrefix("@"),
                    bio = profile.bio,
                    avatarUrl = profile.avatarUrl,
                    avatarInitials = profile.avatarInitials,
                    avatarColor = profile.avatarColor,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = EditProfileUiState(
                    isLoading = false,
                    error = "Không thể tải hồ sơ: ${e.message}",
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, error = null) }
    }

    fun onUsernameChange(value: String) {
        val cleaned = value.trim().lowercase().removePrefix("@")
        _uiState.update { it.copy(username = cleaned, error = null) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(bio = value, error = null) }
    }

    fun onAvatarPicked(bytes: ByteArray) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        _uiState.update { it.copy(isUploadingAvatar = true, error = null) }
        viewModelScope.launch {
            try {
                val url = repository.uploadAvatar(bytes, userId)
                _uiState.update { it.copy(avatarUrl = url, isUploadingAvatar = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isUploadingAvatar = false,
                        error = "Không thể tải ảnh lên: ${e.message}",
                    )
                }
            }
        }
    }

    fun onAvatarPickError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val usernameError = validateUsername(state.username)
        if (usernameError != null) {
            _uiState.update { it.copy(error = usernameError) }
            return
        }
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Tên hiển thị không được để trống") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.updateProfile(
                    name = state.name.trim(),
                    username = state.username.trim(),
                    bio = state.bio.trim(),
                    avatarUrl = state.avatarUrl.takeIf { it.isNotBlank() },
                )
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Không thể lưu hồ sơ. Vui lòng thử lại.",
                    )
                }
            }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun validateUsername(username: String): String? {
        if (username.isBlank()) return "Username không được để trống"
        if (username.length !in 3..30) return "Username phải từ 3–30 ký tự"
        if (!username.matches(Regex("^[a-z0-9._]+$"))) {
            return "Username chỉ gồm chữ thường, số, dấu chấm hoặc gạch dưới"
        }
        return null
    }
}

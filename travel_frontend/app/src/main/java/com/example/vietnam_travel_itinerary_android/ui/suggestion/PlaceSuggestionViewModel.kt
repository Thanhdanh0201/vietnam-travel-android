package com.example.vietnam_travel_itinerary_android.ui.suggestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.api.RetrofitInstance
import com.example.vietnam_travel_itinerary_android.data.dto.PlaceSuggestionRequest
import com.example.vietnam_travel_itinerary_android.data.dto.PlaceSuggestionResponse
import com.example.vietnam_travel_itinerary_android.data.model.Province
import com.example.vietnam_travel_itinerary_android.data.repository.PlaceSuggestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceSuggestionViewModel(
    private val repository: PlaceSuggestionRepository,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val suggestions: List<PlaceSuggestionResponse> = emptyList(),
        val provinces: List<Province> = emptyList(),
        val isSubmitting: Boolean = false,
        val submitSuccess: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadMySuggestions() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val list = repository.getMySuggestions()
                _uiState.update { it.copy(isLoading = false, suggestions = list) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = "Không thể tải danh sách đề xuất.") }
            }
        }
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            try {
                val list = repository.getMySuggestions()
                _uiState.update { it.copy(suggestions = list) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(error = "Không thể làm mới.") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun loadProvinces() {
        if (_uiState.value.provinces.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val provinces = RetrofitInstance.api.getProvinces()
                _uiState.update { it.copy(provinces = provinces) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submit(
        name: String,
        provinceId: String?,
        type: String?,
        description: String?,
        imageBytes: ByteArray?,
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập tên địa điểm.") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, error = null, submitSuccess = false) }
        viewModelScope.launch {
            try {
                val imageUrl = imageBytes?.let { repository.uploadImage(it, "suggestion.jpg") }
                repository.createSuggestion(
                    PlaceSuggestionRequest(
                        name = name.trim(),
                        provinceId = provinceId,
                        type = type?.takeIf { it.isNotBlank() },
                        description = description?.takeIf { it.isNotBlank() },
                        imageUrl = imageUrl,
                    )
                )
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isSubmitting = false, error = "Gửi đề xuất thất bại. Vui lòng thử lại.") }
            }
        }
    }

    fun consumeSubmitSuccess() {
        _uiState.update { it.copy(submitSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

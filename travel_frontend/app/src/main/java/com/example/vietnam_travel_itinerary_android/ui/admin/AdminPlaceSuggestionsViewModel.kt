package com.example.vietnam_travel_itinerary_android.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.dto.PlaceSuggestionResponse
import com.example.vietnam_travel_itinerary_android.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminPlaceSuggestionsViewModel(
    private val repository: AdminRepository,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val status: String = "pending",
        val query: String = "",
        val suggestions: List<PlaceSuggestionResponse> = emptyList(),
        val actionInProgressId: String? = null,
        val error: String? = null,
        val message: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val visibleSuggestions: List<PlaceSuggestionResponse>
        get() {
            val q = _uiState.value.query.trim().lowercase()
            val list = _uiState.value.suggestions
            return if (q.isBlank()) list
            else list.filter {
                (it.name?.lowercase()?.contains(q) == true) ||
                    (it.provinceName?.lowercase()?.contains(q) == true) ||
                    (it.userName?.lowercase()?.contains(q) == true)
            }
        }

    fun load(status: String = _uiState.value.status) {
        _uiState.update { it.copy(isLoading = true, status = status, error = null) }
        viewModelScope.launch {
            try {
                val list = repository.getPlaceSuggestions(status)
                _uiState.update { it.copy(isLoading = false, suggestions = list) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = "Không thể tải đề xuất.") }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                val list = repository.getPlaceSuggestions(_uiState.value.status)
                _uiState.update { it.copy(suggestions = list) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Không thể làm mới.") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun setQuery(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun approve(id: String) {
        _uiState.update { it.copy(actionInProgressId = id, error = null) }
        viewModelScope.launch {
            val ok = try {
                repository.approveSuggestion(id)
            } catch (e: Exception) {
                false
            }
            if (ok) {
                _uiState.update {
                    it.copy(
                        actionInProgressId = null,
                        suggestions = it.suggestions.filter { s -> s.id != id },
                        message = "Đã duyệt đề xuất.",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        actionInProgressId = null,
                        error = "Không thể duyệt. Đề xuất cần có tỉnh và loại địa điểm.",
                    )
                }
            }
        }
    }

    fun reject(id: String, adminNote: String?) {
        _uiState.update { it.copy(actionInProgressId = id, error = null) }
        viewModelScope.launch {
            val ok = try {
                repository.rejectSuggestion(id, adminNote)
            } catch (e: Exception) {
                false
            }
            if (ok) {
                _uiState.update {
                    it.copy(
                        actionInProgressId = null,
                        suggestions = it.suggestions.filter { s -> s.id != id },
                        message = "Đã từ chối đề xuất.",
                    )
                }
            } else {
                _uiState.update { it.copy(actionInProgressId = null, error = "Không thể từ chối.") }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

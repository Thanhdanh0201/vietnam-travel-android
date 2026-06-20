package com.example.vietnam_travel_itinerary_android.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.dto.AdminReportResponse
import com.example.vietnam_travel_itinerary_android.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminReportsViewModel(
    private val repository: AdminRepository,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val status: String = "pending",
        val reports: List<AdminReportResponse> = emptyList(),
        val actionInProgressId: String? = null,
        val error: String? = null,
        val message: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load(status: String = _uiState.value.status) {
        _uiState.update { it.copy(isLoading = true, status = status, error = null) }
        viewModelScope.launch {
            try {
                val list = repository.getReports(status)
                _uiState.update { it.copy(isLoading = false, reports = list) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = "Không thể tải báo cáo.") }
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                val list = repository.getReports(_uiState.value.status)
                _uiState.update { it.copy(reports = list) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Không thể làm mới.") }
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun runAction(id: String, successMsg: String, block: suspend () -> AdminRepository.AdminActionResult) {
        _uiState.update { it.copy(actionInProgressId = id, error = null) }
        viewModelScope.launch {
            val result = try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                AdminRepository.AdminActionResult(success = false, errorMessage = e.message)
            }
            if (result.success) {
                val list = try {
                    repository.getReports(_uiState.value.status)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.value.reports.filter { r -> r.id != id }
                }
                _uiState.update {
                    it.copy(
                        actionInProgressId = null,
                        reports = list,
                        message = successMsg,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        actionInProgressId = null,
                        error = result.errorMessage ?: "Thao tác thất bại.",
                    )
                }
            }
        }
    }

    fun dismiss(id: String) = runAction(id, "Đã bỏ qua báo cáo.") { repository.resolveReport(id, "dismissed") }

    fun deletePost(id: String) = runAction(id, "Đã xóa bài viết.") { repository.deleteReportedPost(id) }

    fun deleteComment(id: String) = runAction(id, "Đã xóa bình luận.") { repository.deleteReportedComment(id) }

    fun banUser(reportId: String, userId: String, reason: String?) =
        runAction(reportId, "Đã cấm người dùng.") {
            val banned = repository.banUser(userId, reason)
            if (banned) repository.resolveReport(reportId, "resolved")
            else AdminRepository.AdminActionResult(success = false, errorMessage = "Không thể cấm người dùng.")
        }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

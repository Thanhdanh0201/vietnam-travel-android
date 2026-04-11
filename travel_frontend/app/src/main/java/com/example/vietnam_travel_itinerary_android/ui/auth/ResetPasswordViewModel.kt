package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val supabase: SupabaseClient
) : ViewModel() {

    data class PasswordRequirements(
        val minLengthCount: Boolean = false,
        val hasUpperCase: Boolean = false,
        val hasNumber: Boolean = false
    )

    data class ResetPasswordUiState(
        val newPassword: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null,
        val passwordRequirements: PasswordRequirements = PasswordRequirements()
    )

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onNewPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                newPassword = password,
                error = null,
                passwordRequirements = PasswordRequirements(
                    minLengthCount = password.length >= 8,
                    hasUpperCase = password.any { it.isUpperCase() },
                    hasNumber = password.any { it.isDigit() }
                )
            )
        }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, error = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onResetPasswordClick() {
        val state = _uiState.value

        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }

        if (!state.passwordRequirements.minLengthCount ||
            !state.passwordRequirements.hasUpperCase ||
            !state.passwordRequirements.hasNumber) {
            _uiState.update { it.copy(error = "Vui lòng đáp ứng tất cả yêu cầu mật khẩu") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                supabase.auth.updateUser {
                    password = state.newPassword
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Lỗi: ${e.message}")
                }
            }
        }
    }
}
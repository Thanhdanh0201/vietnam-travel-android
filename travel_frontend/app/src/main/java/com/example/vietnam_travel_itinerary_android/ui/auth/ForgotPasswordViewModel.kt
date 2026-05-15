package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.SupabaseObject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val supabase: SupabaseClient // THÊM DÒNG NÀY
) : ViewModel() {

    enum class ForgotPasswordStep {
        REQUEST_EMAIL, RESET_PASSWORD, SUCCESS
    }

    data class ForgotPasswordUiState(
        val email: String = "",
        val emailError: String? = null,
        val newPassword: String = "",
        val confirmPassword: String = "",
        val newPasswordError: String? = null,
        val confirmPasswordError: String? = null,
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val currentStep: ForgotPasswordStep = ForgotPasswordStep.REQUEST_EMAIL,
        val navigateToOtp: Boolean = false,
        val passwordRequirements: PasswordRequirements = PasswordRequirements()
    )

    data class PasswordRequirements(
        val minLengthCount: Boolean = false,
        val hasUpperCase: Boolean = false,
        val hasNumber: Boolean = false
    )

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { 
            it.copy(
                newPassword = password, 
                newPasswordError = null,
                passwordRequirements = PasswordRequirements(
                    minLengthCount = password.length >= 8,
                    hasUpperCase = password.any { char -> char.isUpperCase() },
                    hasNumber = password.any { char -> char.isDigit() }
                )
            ) 
        }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onRequestEmailClick() {
        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Vui lòng nhập email") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                SupabaseObject.client.auth.resetPasswordForEmail(_uiState.value.email)
                _uiState.update { it.copy(isLoading = false, navigateToOtp = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, emailError = "Email không tồn tại!") }
            }
        }
    }

    fun onResetPasswordClick() {
        val state = _uiState.value
        var hasError = false
        
        if (state.newPassword.length < 8) {
            _uiState.update { it.copy(newPasswordError = "Mật khẩu tối thiểu 8 ký tự") }
            hasError = true
        }
        
        if (state.confirmPassword != state.newPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Mật khẩu không khớp") }
            hasError = true
        }
        
        if (hasError) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                SupabaseObject.client.auth.updateUser {
                    password = state.newPassword
                }
                _uiState.update { it.copy(isLoading = false, currentStep = ForgotPasswordStep.SUCCESS) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, newPasswordError = "Lỗi cập nhật mật khẩu!") }
            }
        }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(navigateToOtp = false) }
    }

    fun setStep(step: ForgotPasswordStep) {
        _uiState.update { it.copy(currentStep = step) }
    }
}

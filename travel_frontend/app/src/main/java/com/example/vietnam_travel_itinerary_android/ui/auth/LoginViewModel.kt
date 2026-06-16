package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.repository.ProfileRepository
import com.example.vietnam_travel_itinerary_android.data.session.UserSessionCache
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val supabase: SupabaseClient,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    data class LoginUiState(
        val email: String = "",
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val emailError: String? = null,
        val passwordError: String? = null,
        val loginSuccess: Boolean = false,
        val generalError: String? = null
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                generalError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                generalError = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun onLoginClick() {
        val state = _uiState.value

        // Validate email
        val emailError = when {
            state.email.isBlank() -> "Vui lòng nhập email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches() ->
                "Email không đúng định dạng"
            else -> null
        }

        // Validate password
        val passwordError = when {
            state.password.isBlank() -> "Vui lòng nhập mật khẩu"
            state.password.length < 6 -> "Mật khẩu sai hoặc tài khoản không tồn tại"
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        // Proceed with login
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // GỌI SUPABASE THỰC TẾ
                supabase.auth.signInWith(Email) {
                    email = state.email
                    password = state.password
                }

                delay(300)
                try {
                    profileRepository.loadSessionProfile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Sai email hoặc mật khẩu!"
                    )
                }
            }
        }
    }

    fun onGoogleSignInClick() {
        // TODO: Implement Google Sign-In
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                delay(1000) // Simulate
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Đăng nhập Google thất bại."
                    )
                }
            }
        }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}

package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.api.VietnamTravelApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
class RegisterViewModel(
    private val supabase: SupabaseClient,
    private val api: VietnamTravelApi
) : ViewModel() {

    data class RegisterUiState(
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val agreesToTerms: Boolean = false,
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val firstNameError: String? = null,
        val lastNameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val termsError: String? = null,
        val navigateToOtp: Boolean = false,
        val generalError: String? = null
    )

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFirstNameChange(name: String) {
        _uiState.update { it.copy(firstName = name, firstNameError = null) }
    }

    fun onLastNameChange(name: String) {
        _uiState.update { it.copy(lastName = name, lastNameError = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun onTermsAgreeChange(agrees: Boolean) {
        _uiState.update { it.copy(agreesToTerms = agrees, termsError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onRegisterClick() {
        val state = _uiState.value

        // Simple Validation
        val firstNameError = if (state.firstName.isBlank()) "Vui lòng nhập họ" else null
        val lastNameError = if (state.lastName.isBlank()) "Vui lòng nhập tên" else null
        val emailError = if (state.email.isBlank()) "Vui lòng nhập email" else null
        val passwordError = if (state.password.length < 8) "Mật khẩu tối thiểu 8 ký tự" else null
        val confirmPasswordError = if (state.confirmPassword != state.password) "Mật khẩu không khớp" else null
        val termsError = if (!state.agreesToTerms) "Vui lòng đồng ý với điều khoản" else null

        if (firstNameError != null || lastNameError != null || emailError != null ||
            passwordError != null || confirmPasswordError != null || termsError != null) {
            _uiState.update {
                it.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                    termsError = termsError
                )
            }
            return
        }

        // Start loading
        _uiState.update { it.copy(isLoading = true, generalError = null) }

        viewModelScope.launch {
            try {
                // Step 1: Sign up with Supabase
                supabase.auth.signUpWith(Email) {
                    email = state.email
                    password = state.password
                }

                // Step 2: Get access token
                val token = supabase.auth.currentAccessTokenOrNull()

                if (token != null) {
                    // Step 3: Call Spring Boot API to sync user
                    val response = api.syncUser("Bearer $token")

                    if (response.isSuccessful) {
                        // Sync success -> Move to OTP screen
                        _uiState.update { it.copy(isLoading = false, navigateToOtp = true) }
                    } else {
                        // Sync failed
                        _uiState.update {
                            it.copy(isLoading = false, generalError = "Sync Failed: Code ${response.code()}")
                        }
                    }
                } else {
                    // Token is null (e.g. Supabase requires email confirmation first)
                    // Still move to OTP screen to verify
                    _uiState.update { it.copy(isLoading = false, navigateToOtp = true) }
                }

            } catch (e: Exception) {
                // Network or Supabase error
                _uiState.update {
                    it.copy(isLoading = false, generalError = "Registration Error: ${e.message}")
                }
            }
        }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(navigateToOtp = false) }
    }
}

package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpViewModel : ViewModel() {

    enum class OtpStep {
        OTP, SUCCESS
    }

    data class OtpUiState(
        val email: String = "",
        val otpCode: String = "",
        val otpError: String? = null,
        val timerSeconds: Int = 59,
        val isLoading: Boolean = false,
        val currentStep: OtpStep = OtpStep.OTP,
        val verificationType: VerificationType = VerificationType.REGISTER
    )

    enum class VerificationType {
        REGISTER, FORGOT_PASSWORD
    }

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    fun init(email: String, type: VerificationType) {
        _uiState.update { it.copy(email = email, verificationType = type) }
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            _uiState.update { it.copy(timerSeconds = 59) }
            while (_uiState.value.timerSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }
        }
    }

    fun onOtpChange(otp: String) {
        if (otp.length <= 4) {
            _uiState.update { it.copy(otpCode = otp, otpError = null) }
            if (otp.length == 4) {
                onVerifyOtp()
            }
        }
    }

    fun onVerifyOtp() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            delay(1500) // Simulate API
            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentStep = OtpStep.SUCCESS
                )
            }
        }
    }

    fun onResendOtp() {
        if (_uiState.value.timerSeconds == 0) {
            startTimer()
            // TODO: Call API to resend OTP
        }
    }
}

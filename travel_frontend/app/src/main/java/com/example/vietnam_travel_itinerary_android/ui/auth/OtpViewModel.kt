package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vietnam_travel_itinerary_android.data.api.VietnamTravelApi
import com.example.vietnam_travel_itinerary_android.data.model.UserSyncRequest
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpViewModel(
    private val supabase: SupabaseClient,
    private val api: VietnamTravelApi
) : ViewModel() {

    enum class OtpStep {
        OTP, SUCCESS
    }

    enum class VerificationType {
        REGISTER, FORGOT_PASSWORD
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

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    // Biến giữ tên thật từ màn hình Register truyền sang để tránh bị EMPTY
    private var savedFullName: String = ""

    // Hàm init duy nhất để khởi tạo dữ liệu
    fun init(email: String, name: String, type: VerificationType) {
        _uiState.update { it.copy(email = email, verificationType = type) }
        this.savedFullName = name
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
        if (otp.length <= 6) {
            _uiState.update { it.copy(otpCode = otp, otpError = null) }
            if (otp.length == 6) {
                onVerifyOtp()
            }
        }
    }

    fun onVerifyOtp() {
        val state = _uiState.value

        if (state.otpCode.length < 6) {
            _uiState.update { it.copy(otpError = "Please enter all 6 digits") }
            return
        }

        _uiState.update { it.copy(isLoading = true, otpError = null) }

        viewModelScope.launch {
            try {
                // 1. Xác thực OTP với Supabase
                println("OTP: Bắt đầu xác thực OTP cho email: ${state.email}")
                supabase.auth.verifyEmailOtp(
                    type = if (state.verificationType == VerificationType.REGISTER) OtpType.Email.SIGNUP else OtpType.Email.RECOVERY,
                    email = state.email,
                    token = state.otpCode
                )
                println("OTP: Xác thực Supabase thành công!")

                // Đợi một chút để Session ổn định
                delay(500)

                // 2. Đồng bộ với Spring Boot (Chỉ khi Đăng ký mới)
                if (state.verificationType == VerificationType.REGISTER) {
                    try {
                        val user = supabase.auth.currentUserOrNull()
                        val token = supabase.auth.currentAccessTokenOrNull()

                        println("OTP: User = ${user?.id}, Token có = ${token != null}")

                        if (token != null && user != null) {
                            // Tạo request với tên thật đã lưu từ bước init
                            val syncRequest = UserSyncRequest(
                                id = user.id,
                                email = user.email ?: "",
                                name = if (savedFullName.isNotBlank()) savedFullName else "Traveler"
                            )

                            // Gọi API đồng bộ
                            val response = api.syncUser(
                                token = "Bearer $token",
                                request = syncRequest
                            )

                            if (response.isSuccessful) {
                                println("OTP: Sync thành công tới Backend với tên: $savedFullName")
                            } else {
                                println("OTP: Sync thất bại. Mã lỗi: ${response.code()}, Body: ${response.errorBody()?.string()}")
                            }
                        } else {
                            println("OTP: Lỗi - Không tìm thấy Session để Sync. User: ${user?.id}, Token: $token")
                        }
                    } catch (syncError: Exception) {
                        // Lỗi sync không nên chặn flow thành công của OTP
                        println("OTP: Lỗi khi sync với Backend (không ảnh hưởng flow): ${syncError.message}")
                        syncError.printStackTrace()
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentStep = OtpStep.SUCCESS
                    )
                }

            } catch (e: Exception) {
                println("OTP Error: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        otpError = "Mã OTP không hợp lệ hoặc đã hết hạn"
                    )
                }
            }
        }
    }

    fun onResendOtp() {
        if (_uiState.value.timerSeconds == 0) {
            startTimer()
            viewModelScope.launch {
                try {
                    if (_uiState.value.verificationType == VerificationType.REGISTER) {
                        supabase.auth.resendEmail(
                            type = OtpType.Email.SIGNUP,
                            email = _uiState.value.email
                        )
                    } else {
                        supabase.auth.resetPasswordForEmail(_uiState.value.email)
                    }
                } catch (e: Exception) {
                    println("Resend OTP failed: ${e.message}")
                }
            }
        }
    }
}
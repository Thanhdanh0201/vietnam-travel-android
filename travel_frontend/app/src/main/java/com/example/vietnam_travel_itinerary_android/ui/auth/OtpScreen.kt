package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.ui.theme.*

@Composable
fun OtpScreen(
    email: String,
    fullName: String, // Nhận thêm fullName từ NavGraph
    type: OtpViewModel.VerificationType = OtpViewModel.VerificationType.REGISTER,
    onBackClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToResetPassword: () -> Unit = {},
    viewModel: OtpViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // SỬA TẠI ĐÂY: Truyền đúng 3 tham số (email, fullName, type) vào hàm init mới
    LaunchedEffect(email, fullName, type) {
        viewModel.init(email, fullName, type)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() }
            )
    ) {
        OtpBackground(step = uiState.currentStep)

        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
            },
            label = "OtpFlow"
        ) { step ->
            when (step) {
                OtpViewModel.OtpStep.OTP -> {
                    OtpForm(
                        uiState = uiState,
                        onOtpChange = viewModel::onOtpChange,
                        onResendOtp = viewModel::onResendOtp,
                        onVerifyOtp = viewModel::onVerifyOtp,
                        onBackClick = onBackClick
                    )
                }
                OtpViewModel.OtpStep.SUCCESS -> {
                    OtpSuccessForm(
                        type = uiState.verificationType,
                        onPrimaryClick = {
                            if (uiState.verificationType == OtpViewModel.VerificationType.REGISTER) {
                                onNavigateToLogin()
                            } else {
                                onNavigateToResetPassword()
                            }
                        }
                    )
                }
            }
        }
    }
}

// --- Các hàm Composable phụ bên dưới giữ nguyên ---

@Composable
private fun OtpBackground(step: OtpViewModel.OtpStep) {
    when (step) {
        OtpViewModel.OtpStep.OTP -> {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F6F6)))
        }
        OtpViewModel.OtpStep.SUCCESS -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val spacing = 16.dp.toPx()
                    val alpha = 0.03f
                    for (x in 0..(size.width / spacing).toInt()) {
                        for (y in 0..(size.height / spacing).toInt()) {
                            drawCircle(
                                color = VNRed.copy(alpha = alpha),
                                radius = 1.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x * spacing, y * spacing)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpForm(
    uiState: OtpViewModel.OtpUiState,
    onOtpChange: (String) -> Unit,
    onResendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp).clip(CircleShape)) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Back", tint = VNRed)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(16.dp).widthIn(max = 448.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(elevation = 15.dp, shape = RoundedCornerShape(24.dp)),
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.width(6.dp).height(24.dp).background(VNRed, CircleShape))
                            Text(
                                text = "Xác thực tài khoản",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF0F172A),
                                letterSpacing = (-0.5).sp
                            )
                        }
                        Text(
                            text = "Mã xác thực đã được gửi đến email ${uiState.email}. Vui lòng kiểm tra hộp thư và nhập mã 6 chữ số bên dưới.",
                            fontFamily = BeVietnamPro,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    OtpInputField(otpCode = uiState.otpCode, onOtpChange = onOtpChange)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.timerSeconds > 0) {
                            Text(
                                text = "GỬI LẠI MÃ SAU 00:${uiState.timerSeconds.toString().padStart(2, '0')}",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = VNRed,
                                letterSpacing = 1.2.sp
                            )
                        } else {
                            Text(
                                text = "GỬI LẠI MÃ",
                                fontFamily = BeVietnamPro,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.clickable { onResendOtp() }
                            )
                        }
                    }

                    Button(
                        onClick = { if (uiState.otpCode.length == 6) onVerifyOtp() },
                        modifier = Modifier.fillMaxWidth().height(52.dp).shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp), spotColor = VNRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VNRed, contentColor = Color.White),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = "XÁC NHẬN", fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.4.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpSuccessForm(type: OtpViewModel.VerificationType, onPrimaryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(96.dp).shadow(elevation = 20.dp, shape = CircleShape, spotColor = VNRed.copy(alpha = 0.2f)).background(VNRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
            val title = if (type == OtpViewModel.VerificationType.REGISTER) "Đăng ký thành công!" else "Xác thực thành công!"
            Text(text = title, fontFamily = BeVietnamPro, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, color = Color(0xFF1A1A1A))
            Text(
                text = if (type == OtpViewModel.VerificationType.REGISTER) "Tài khoản đã sẵn sàng. Khám phá ngay!" else "Mã chính xác. Đặt lại mật khẩu thôi!",
                fontFamily = BeVietnamPro, fontSize = 14.sp, color = Color(0xFF666666), textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxWidth().widthIn(max = 346.dp).height(56.dp).shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VNRed)
        ) {
            Text(text = if (type == OtpViewModel.VerificationType.REGISTER) "Quay lại đăng nhập" else "Tiếp tục", fontFamily = BeVietnamPro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun OtpInputField(otpCode: String, onOtpChange: (String) -> Unit) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
        for (i in 0 until 6) {
            val char = otpCode.getOrNull(i)?.toString() ?: ""
            Surface(
                modifier = Modifier.size(width = 42.dp, height = 56.dp).border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp)).shadow(elevation = 4.dp),
                color = Color.White, shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    BasicTextField(
                        value = char,
                        onValueChange = {
                            if (it.length <= 1) {
                                val currentCode = otpCode.toMutableList()
                                if (it.isEmpty()) { if (currentCode.size > i) currentCode.removeAt(i) }
                                else { if (currentCode.size > i) currentCode[i] = it[0] else currentCode.add(it[0]) }
                                onOtpChange(currentCode.joinToString(""))
                                if (it.isNotEmpty() && i < 5) focusRequesters[i + 1].requestFocus()
                            }
                        },
                        textStyle = TextStyle(fontFamily = BeVietnamPro, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[i])
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) { focusRequesters[0].requestFocus() }
}
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vietnam_travel_itinerary_android.ui.theme.*

@Composable
fun ResetPasswordScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Set initial step to RESET_PASSWORD when this screen is opened
    LaunchedEffect(Unit) {
        viewModel.setStep(ForgotPasswordViewModel.ForgotPasswordStep.RESET_PASSWORD)
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
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                fadeOut(animationSpec = tween(500))
            },
            label = "ResetPasswordFlow"
        ) { step ->
            when (step) {
                ForgotPasswordViewModel.ForgotPasswordStep.RESET_PASSWORD, 
                ForgotPasswordViewModel.ForgotPasswordStep.REQUEST_EMAIL -> {
                    ResetPasswordForm(
                        uiState = uiState,
                        onPasswordChange = viewModel::onPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        onTogglePassword = viewModel::togglePasswordVisibility,
                        onToggleConfirmPassword = viewModel::toggleConfirmPasswordVisibility,
                        onResetClick = viewModel::onResetPasswordClick
                    )
                }
                ForgotPasswordViewModel.ForgotPasswordStep.SUCCESS -> {
                    ResetSuccessScreen(onBackToLogin = onNavigateToLogin)
                }
            }
        }
    }
}

@Composable
private fun ResetPasswordForm(
    uiState: ForgotPasswordViewModel.ForgotPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleConfirmPassword: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F6))
            .padding(horizontal = 24.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.width(6.dp).height(24.dp).background(VNRed, CircleShape))
                Text(
                    text = "Đặt lại mật khẩu",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.6).sp
                )
            }
            Text(
                text = "Tạo mật khẩu mới cho tài khoản của bạn.",
                fontFamily = BeVietnamPro,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Form
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // New Password
            ResetPasswordField(
                value = uiState.newPassword,
                onValueChange = onPasswordChange,
                label = "MẬT KHẨU MỚI",
                placeholder = "••••••••",
                isPasswordVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePassword,
                error = uiState.newPasswordError
            )

            // Confirm Password
            ResetPasswordField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "XÁC NHẬN MẬT KHẨU",
                placeholder = "••••••••",
                isPasswordVisible = uiState.isConfirmPasswordVisible,
                onToggleVisibility = onToggleConfirmPassword,
                error = uiState.confirmPasswordError
            )

            // Requirements List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VNRed.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequirementItem(text = "Tối thiểu 8 ký tự", isMet = uiState.passwordRequirements.minLengthCount)
                RequirementItem(text = "Bao gồm chữ hoa", isMet = uiState.passwordRequirements.hasUpperCase)
                RequirementItem(text = "Bao gồm ít nhất một chữ số", isMet = uiState.passwordRequirements.hasNumber)
            }

            // Submit Button
            Button(
                onClick = onResetClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), spotColor = VNRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XÁC NHẬN",
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RequirementItem(text: String, isMet: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (isMet) Color(0xFF16A34A) else Color(0xFFCBD5E1),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            fontFamily = BeVietnamPro,
            fontSize = 12.sp,
            color = Color(0xFF475569)
        )
    }
}

@Composable
private fun ResetPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    error: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color(0xFF475569),
            letterSpacing = 1.sp
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
            color = Color.White,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = BeVietnamPro,
                            fontSize = 16.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        ),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = if (isPasswordVisible) "ẨN" else "HIỆN",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = VNRed,
                    modifier = Modifier.clickable { onToggleVisibility() }
                )
            }
        }
        if (error != null) {
            Text(text = error, color = Color.Red, fontSize = 10.sp, fontFamily = BeVietnamPro)
        }
    }
}

@Composable
private fun ResetSuccessScreen(onBackToLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Subtle Pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 16.dp.toPx()
            for (x in 0..(size.width / spacing).toInt()) {
                for (y in 0..(size.height / spacing).toInt()) {
                    drawCircle(color = VNRed.copy(alpha = 0.02f), radius = 1.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x * spacing, y * spacing))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(elevation = 20.dp, shape = CircleShape, spotColor = VNRed.copy(alpha = 0.2f))
                    .background(VNRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Đổi mật khẩu thành công!",
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                letterSpacing = (-0.75).sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mật khẩu của bạn đã được cập nhật. Hãy sử dụng mật khẩu mới để đăng nhập vào tài khoản.",
                fontFamily = BeVietnamPro,
                fontSize = 14.sp,
                lineHeight = 23.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onBackToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 346.dp)
                    .height(56.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp), spotColor = VNRed.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VNRed)
            ) {
                Text(
                    text = "Quay lại đăng nhập",
                    fontFamily = BeVietnamPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

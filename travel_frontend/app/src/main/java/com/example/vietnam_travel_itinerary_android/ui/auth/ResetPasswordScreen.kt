package com.example.vietnam_travel_itinerary_android.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    viewModel: ResetPasswordViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

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
            targetState = uiState.isSuccess,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith
                        fadeOut(animationSpec = tween(500))
            },
            label = "ResetPasswordFlow"
        ) { isSuccess ->
            if (isSuccess) {
                ResetSuccessScreen(onBackToLogin = onNavigateToLogin)
            } else {
                ResetPasswordForm(
                    uiState = uiState,
                    onPasswordChange = viewModel::onNewPasswordChange,
                    onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                    onTogglePassword = viewModel::togglePasswordVisibility,
                    onToggleConfirmPassword = viewModel::toggleConfirmPasswordVisibility,
                    onResetClick = viewModel::onResetPasswordClick
                )
            }
        }
    }
}

@Composable
private fun ResetPasswordForm(
    uiState: ResetPasswordViewModel.ResetPasswordUiState,
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.width(6.dp).height(24.dp).background(VNRed, CircleShape))
            Text(
                text = "Đặt lại mật khẩu",
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            ResetPasswordField(
                value = uiState.newPassword,
                onValueChange = onPasswordChange,
                label = "MẬT KHẨU MỚI",
                placeholder = "••••••••",
                isPasswordVisible = uiState.isPasswordVisible,
                onToggleVisibility = onTogglePassword
            )

            ResetPasswordField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "XÁC NHẬN MẬT KHẨU",
                placeholder = "••••••••",
                isPasswordVisible = uiState.isConfirmPasswordVisible,
                onToggleVisibility = onToggleConfirmPassword,
                error = uiState.error
            )

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

            Button(
                onClick = onResetClick,
                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(elevation = 10.dp, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "XÁC NHẬN",
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RequirementItem(text: String, isMet: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (isMet) Color(0xFF16A34A) else Color(0xFFCBD5E1),
            modifier = Modifier.size(12.dp)
        )
        Text(text = text, fontFamily = BeVietnamPro, fontSize = 12.sp, color = Color(0xFF475569))
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
            color = if (error != null) Color.Red else Color(0xFF475569)
        )
        Surface(
            modifier = Modifier.fillMaxWidth().height(56.dp).shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp)),
            color = Color.White, shape = RoundedCornerShape(12.dp),
            border = if (error != null) androidx.compose.foundation.BorderStroke(1.dp, Color.Red) else null
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                Box(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    if (value.isEmpty()) Text(text = placeholder, fontFamily = BeVietnamPro, fontSize = 16.sp, color = Color(0xFF94A3B8))
                    BasicTextField(
                        value = value, onValueChange = onValueChange, singleLine = true,
                        textStyle = TextStyle(fontFamily = BeVietnamPro, fontSize = 16.sp, color = Color(0xFF0F172A)),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
        if (error != null) Text(text = error, color = Color.Red, fontSize = 10.sp, fontFamily = BeVietnamPro)
    }
}

@Composable
private fun ResetSuccessScreen(onBackToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(96.dp).background(VNRed, CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Đổi mật khẩu thành công!",
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Mật khẩu của bạn đã được cập nhật. Hãy sử dụng mật khẩu mới để đăng nhập.",
            fontFamily = BeVietnamPro,
            fontSize = 14.sp,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
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